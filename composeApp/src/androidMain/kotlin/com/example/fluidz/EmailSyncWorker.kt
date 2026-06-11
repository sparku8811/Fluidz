package com.example.fluidz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.microsoft.graph.models.Message
import com.microsoft.graph.requests.GraphServiceClient
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class EmailSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "fluidz_notifications"
        private const val NOTIFICATION_ID = 1001
    }

    private var accessToken: String? = null

    private val defaultVaSenders = listOf(
        "video.appointment@va.gov",
        "VaNoReplymessages@va.gov",
        "veteransaffairs@messages.va.gov",
        "veteranshealth@messages.va.gov",
        "do-not-reply@notifications.va.gov",
        "VHATMPSchedulingD@va.gov",
    )

    override suspend fun doWork(): Result {
        val sharedPrefs = SecurityUtils.getEncryptedSharedPreferences(applicationContext)
        val userEmail = sharedPrefs.getString("user_email", "")
        val customSenders = sharedPrefs.getStringSet("custom_senders", emptySet()) ?: emptySet()
        
        if (userEmail.isNullOrEmpty()) {
            return Result.failure()
        }

        if (accessToken == null) return Result.retry()

        val graphClient = GraphServiceClient.builder()
            .authenticationProvider { _: URL ->
                val future = CompletableFuture<String>()
                future.complete(accessToken)
                future
            }
            .buildClient()

        try {
            val allSenders = defaultVaSenders + customSenders
            val filter = allSenders.joinToString(" or ") { "from/emailAddress/address eq '$it'" }
            
            val messages = graphClient.me().messages()
                .buildRequest()
                .filter(filter)
                .get()

            messages?.currentPage?.forEach { message ->
                val body = message.body?.content ?: ""
                // Step 2: Privacy Cleansing
                if (!isPrivacyRisk(body)) {
                    parseAndAddAppointment(applicationContext, message)
                    pushToSyncedDevices(applicationContext, message)
                }
            }
        } catch (e: Exception) {
            Log.e("EmailSyncWorker", "Error during sync", e)
            return Result.failure()
        }

        return Result.success()
    }

    private fun isPrivacyRisk(body: String): Boolean {
        val riskKeywords = listOf("password reset", "verification code", "OTP", "marketing", "unsubscribe", "sale ends")
        return riskKeywords.any { body.contains(it, ignoreCase = true) }
    }

    private fun pushToSyncedDevices(context: Context, message: Message) {
        val syncedDevices = DeviceSyncManager.getSyncedDevices(context)
        if (syncedDevices.isNotEmpty()) {
            Log.d("EmailSyncWorker", "Pushing appointment '${message.subject}' to ${syncedDevices.size} devices")
        }
    }

    private fun parseAndAddAppointment(context: Context, message: Message) {
        val body = message.body?.content ?: ""
        val subject = message.subject ?: "Appointment"
        val sender = message.from?.emailAddress?.address ?: "Unknown Source"

        val personBeingSeen = extractValue(body, "(?i)(?:provider|doctor|with|practitioner|host):\\s*(.*)") ?: "Host"
        val reason = extractValue(body, "(?i)(?:reason|purpose|appointment type|subject):\\s*(.*)") ?: subject
        val location = extractValue(body, "(?i)(?:location|facility|address|place|room):\\s*(.*)") ?: "TBD"
        val dateStr = extractValue(body, "(?i)(?:date):\\s*(.*)")
        val timeStr = extractValue(body, "(?i)(?:time):\\s*(.*)")
        val timeZoneStr = extractValue(body, "(?i)(?:timezone|zone):\\s*(.*)") ?: TimeZone.getDefault().id
        val contactInfo = extractValue(body, "(?i)(?:contact|phone|call|email):\\s*(.*)") ?: sender

        // Scraping Links
        val meetingLink = extractLink(body)
        val finalLocation = if (meetingLink != null) "$location ($meetingLink)" else location

        val startMillis = calculateMillis(dateStr, timeStr, timeZoneStr)
        val endMillis = startMillis + 3600000 

        // Detect Intent: Cancellation
        val isCancellation = body.contains("cancel", ignoreCase = true) || 
                            body.contains("can't make it", ignoreCase = true) ||
                            subject.contains("cancelled", ignoreCase = true)
        
        if (isCancellation) {
            AndroidCalendarManager.deleteFluidzEvent(context, reason, startMillis)
            sendNotification(context, "Appointment Removed", "Cancelled: $reason")
            return
        }

        // Handle Upsert
        val description = buildString {
            append("With: $personBeingSeen\n")
            append("Contact: $contactInfo\n")
            if (message.hasAttachments == true) {
                append("📎 This email contains attachments (Itinerary/Tickets).\n")
            }
            append("Automatically imported by Fluidz from $sender")
        }

        val conflictWith = AndroidCalendarManager.checkForConflict(context, startMillis, endMillis)
        val finalTitle = if (conflictWith != null) "[CONFLICT] $reason" else reason

        val eventId = AndroidCalendarManager.upsertFluidzEvent(
            context = context,
            title = finalTitle,
            location = finalLocation,
            description = description,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            isPending = true // All new events start as drafts
        )

        if (eventId != -1L) {
            val notificationBody = if (conflictWith != null) {
                "⚠️ Conflict with '$conflictWith'. Review required."
            } else {
                "$reason at $finalLocation"
            }
            sendNotification(context, "New Draft Appointment", notificationBody, eventId.toString())
        }
    }

    private fun extractLink(text: String): String? {
        val patterns = listOf(
            "https://[\\w\\-]+\\.zoom\\.us/j/\\d+",
            "https://teams\\.microsoft\\.com/l/meetup-join/[\\w%\\-\\./]+",
            "https://meet\\.google\\.com/[a-z]{3}-[a-z]{4}-[a-z]{3}"
        )
        for (pattern in patterns) {
            val matcher = Pattern.compile(pattern).matcher(text)
            if (matcher.find()) return matcher.group(0)
        }
        return null
    }

    private fun sendNotification(context: Context, title: String, message: String, eventId: String? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fluidz Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for captured VA appointments"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (eventId != null) {
            val approveIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = "APPROVE_EVENT"
                putExtra("event_id", eventId)
            }
            val approvePendingIntent = android.app.PendingIntent.getBroadcast(
                context, eventId.hashCode(), approveIntent, 
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.checkbox_on_background, "Approve", approvePendingIntent)
        }

        notificationManager.notify(eventId?.hashCode() ?: NOTIFICATION_ID, builder.build())
    }

    private fun extractValue(text: String, pattern: String): String? {
        val matcher = Pattern.compile(pattern).matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun calculateMillis(date: String?, time: String?, timeZone: String? = null): Long {
        if ((date == null) || (time == null)) return System.currentTimeMillis()
        return try {
            val parts = date.split(Regex("[/\\-\\s]"))
            val timeParts = time.split(":")
            
            // Map common time zone abbreviations to standard IDs
            val tzId = when (timeZone?.uppercase()) {
                "BST" -> "Europe/London"
                "GMT" -> "GMT"
                "EST" -> "America/New_York"
                "EDT" -> "America/New_York"
                "CST" -> "America/Chicago"
                "CDT" -> "America/Chicago"
                "MST" -> "America/Denver"
                "MDT" -> "America/Denver"
                "PST" -> "America/Los_Angeles"
                "PDT" -> "America/Los_Angeles"
                else -> timeZone ?: TimeZone.getDefault().id
            }

            val sourceTz = TimeZone.getTimeZone(tzId)
            val cal = Calendar.getInstance(sourceTz).apply {
                if (parts[0].length == 4) { // YYYY-MM-DD
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                } else { // MM/DD/YYYY
                    set(parts[2].toInt(), parts[0].toInt() - 1, parts[1].toInt())
                }
                set(Calendar.HOUR_OF_DAY, timeParts[0].filter { it.isDigit() }.toInt())
                set(Calendar.MINUTE, timeParts[1].filter { it.isDigit() }.toInt())
            }
            
            // Return UTC millis which will be correctly displayed in user's local time by the calendar app
            cal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
