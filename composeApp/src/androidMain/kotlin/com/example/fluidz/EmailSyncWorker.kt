package com.example.fluidz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
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
                parseAndAddAppointment(applicationContext, message)
                pushToSyncedDevices(applicationContext, message)
            }
        } catch (e: Exception) {
            Log.e("EmailSyncWorker", "Error during sync", e)
            return Result.failure()
        }

        return Result.success()
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
        val contactInfo = extractValue(body, "(?i)(?:contact|phone|call|email):\\s*(.*)") ?: sender

        val startMillis = calculateMillis(dateStr, timeStr)
        val endMillis = startMillis + 3600000 

        val cr = context.contentResolver
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, reason)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(
                CalendarContract.Events.DESCRIPTION, 
                buildString {
                    append("With: $personBeingSeen\n")
                    append("Contact: $contactInfo\n")
                    append("Automatically imported by Fluidz from $sender")
                },
            )
            put(CalendarContract.Events.CALENDAR_ID, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        try {
            cr.insert(CalendarContract.Events.CONTENT_URI, values)
            sendNotification(context, "New Appointment Captured", "Reason: $reason at $location")
        } catch (e: SecurityException) {
            Log.e("EmailSyncWorker", "Calendar permission missing", e)
        }
    }

    private fun sendNotification(context: Context, title: String, message: String) {
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun extractValue(text: String, pattern: String): String? {
        val matcher = Pattern.compile(pattern).matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun calculateMillis(date: String?, time: String?): Long {
        if ((date == null) || (time == null)) return System.currentTimeMillis()
        return try {
            val parts = date.split(Regex("[/\\-\\s]"))
            val timeParts = time.split(":")
            val cal = Calendar.getInstance().apply {
                if (parts[0].length == 4) { // YYYY-MM-DD
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                } else { // MM/DD/YYYY
                    set(parts[2].toInt(), parts[0].toInt() - 1, parts[1].toInt())
                }
                set(Calendar.HOUR_OF_DAY, timeParts[0].filter { it.isDigit() }.toInt())
                set(Calendar.MINUTE, timeParts[1].filter { it.isDigit() }.toInt())
            }
            cal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
