package com.example.fluidz

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {

    private val vaSmsShortCodes = listOf("611611", "838255", "53313") // Common VA/Gov short codes
    private val CHANNEL_ID = "fluidz_notifications"
    private val NOTIFICATION_ID = 1002

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs = SecurityUtils.getEncryptedSharedPreferences(context)
        val isEnabled = sharedPrefs.getBoolean("auto_capture_sms", false)
        if (!isEnabled) return

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.originatingAddress ?: ""
                val body = sms.messageBody ?: ""

                Log.d("SmsReceiver", "Received SMS from $sender: $body")

                // Check if it's from a known VA source or contains VA keywords
                if (isVaRelated(sender, body)) {
                    parseAndAddSmsAppointment(context, body, sender)
                }
            }
        }
    }

    private fun isVaRelated(sender: String, body: String): Boolean {
        // Check if sender is in known list
        if (vaSmsShortCodes.any { sender.contains(it) }) return true
        
        // Check for keywords
        val keywords = listOf("VA appointment", "Veterans Affairs", "VA Medical", "Video Visit", "Appointment reminder")
        return keywords.any { body.contains(it, ignoreCase = true) }
    }

    private fun parseAndAddSmsAppointment(context: Context, body: String, sender: String) {
        val personBeingSeen = extractValue(body, "(?i)(?:provider|doctor|with|practitioner|host):\\s*(.*)") ?: "Host"
        val reason = extractValue(body, "(?i)(?:reason|purpose|appointment|visit):\\s*(.*)") ?: "VA Appointment"
        val location = extractValue(body, "(?i)(?:location|facility|address|place|room):\\s*(.*)") ?: "VA Facility"
        val dateStr = extractValue(body, "(?i)(?:date):\\s*(.*)") ?: extractValue(body, "(\\d{1,2}/\\d{1,2}/\\d{4})")
        val timeStr = extractValue(body, "(?i)(?:time):\\s*(.*)") ?: extractValue(body, "(\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm))")
        val contactInfo = extractValue(body, "(?i)(?:contact|phone|call):\\s*(.*)") ?: sender

        val startMillis = calculateMillis(dateStr, timeStr)
        val endMillis = startMillis + 3600000 

        // Detect Intent: Cancellation
        val isCancellation = body.contains("cancel", ignoreCase = true) || 
                            body.contains("can't make it", ignoreCase = true)
        
        if (isCancellation) {
            val deleted = AndroidCalendarManager.deleteFluidzEvent(context, reason, startMillis)
            if (deleted) {
                sendNotification(context, "Appointment Cancelled", "Removed: $reason from your calendar.")
            }
            return
        }

        // Handle Upsert (Create or Update)
        val description = buildString {
            append("With: $personBeingSeen\n")
            append("Contact: $contactInfo\n")
            append("Automatically imported by Fluidz from SMS: $sender")
        }

        val conflictWith = AndroidCalendarManager.checkForConflict(context, startMillis, endMillis)
        val finalTitle = if (conflictWith != null) "[CONFLICT] $reason" else reason

        val eventId = AndroidCalendarManager.upsertFluidzEvent(
            context = context,
            title = finalTitle,
            location = location,
            description = description,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            isPending = true
        )

        if (eventId != -1L) {
            Log.d("SmsReceiver", "Successfully processed appointment from SMS")
            val notificationBody = if (conflictWith != null) {
                "⚠️ Conflict with '$conflictWith'. Review required."
            } else {
                "$reason at $location"
            }
            sendNotification(context, "New Draft Appointment (SMS)", notificationBody, eventId.toString())
        }
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

        try {
            notificationManager.notify(eventId?.hashCode() ?: NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            Log.e("SmsReceiver", "Permission missing for notification", e)
        }
    }

    private fun extractValue(text: String, pattern: String): String? {
        val matcher = Pattern.compile(pattern).matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun calculateMillis(date: String?, time: String?): Long {
        if (date == null || time == null) return System.currentTimeMillis()
        return try {
            val parts = date.split(Regex("[/\\-\\s]"))
            val cal = Calendar.getInstance().apply {
                if (parts[0].length == 4) { // YYYY-MM-DD
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                } else if (parts.size >= 3) { // MM/DD/YYYY or DD/MM/YYYY
                    // Assume MM/DD/YYYY for US VA messages
                    val year = if (parts[2].length == 2) 2000 + parts[2].toInt() else parts[2].toInt()
                    set(year, parts[0].toInt() - 1, parts[1].toInt())
                }
                
                // Handle time
                val cleanTime = time.uppercase().replace(" ", "")
                val isPm = cleanTime.contains("PM")
                val isAm = cleanTime.contains("AM")
                val timeOnly = cleanTime.replace("AM", "").replace("PM", "")
                val timeParts = timeOnly.split(":")
                
                val hour = timeParts[0].filter { it.isDigit() }.toInt()
                val minute = if (timeParts.size > 1) timeParts[1].filter { it.isDigit() }.toInt() else 0
                
                if (isPm && hour < 12) {
                    set(Calendar.HOUR_OF_DAY, hour + 12)
                } else if (isAm && hour == 12) {
                    set(Calendar.HOUR_OF_DAY, 0)
                } else {
                    set(Calendar.HOUR_OF_DAY, hour)
                }
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            cal.timeInMillis
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error calculating millis", e)
            System.currentTimeMillis()
        }
    }
}
