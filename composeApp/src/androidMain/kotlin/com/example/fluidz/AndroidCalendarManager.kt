package com.example.fluidz

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.*

object AndroidCalendarManager {

    private const val FLUIDZ_TAG = "Automatically imported by Fluidz"
    private const val PENDING_TAG = "[PENDING REVIEW] Fluidz Draft"
    private const val VERIFIED_TAG = "[VERIFIED] Fluidz Appointment"

    fun upsertFluidzEvent(
        context: Context,
        title: String,
        location: String,
        description: String,
        startTimeMillis: Long,
        endTimeMillis: Long,
        isPending: Boolean = true
    ): Long {
        val tag = if (isPending) PENDING_TAG else VERIFIED_TAG
        val existingId = findExistingEventId(context, title, startTimeMillis)

        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.DESCRIPTION, "$description\n\n$tag")
            put(CalendarContract.Events.DTSTART, startTimeMillis)
            put(CalendarContract.Events.DTEND, endTimeMillis)
            put(CalendarContract.Events.CALENDAR_ID, 1)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (isPending) {
                put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_TENTATIVE)
            } else {
                put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
            }
        }

        return try {
            if (existingId != -1L) {
                val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingId)
                context.contentResolver.update(updateUri, values, null, null)
                existingId
            } else {
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                ContentUris.parseId(uri!!)
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Upsert failed", e)
            -1L
        }
    }

    fun approveFluidzEvent(context: Context, eventId: String): Boolean {
        val values = ContentValues().apply {
            put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
        }
        
        return try {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId.toLong())
            val cursor = context.contentResolver.query(uri, arrayOf(CalendarContract.Events.DESCRIPTION), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val desc = it.getString(0) ?: ""
                    values.put(CalendarContract.Events.DESCRIPTION, desc.replace(PENDING_TAG, VERIFIED_TAG))
                }
            }
            context.contentResolver.update(uri, values, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun checkForConflict(context: Context, startTime: Long, endTime: Long): String? {
        val projection = arrayOf(CalendarContract.Events.TITLE)
        val selection = "(${CalendarContract.Events.DTSTART} < ?) AND (${CalendarContract.Events.DTEND} > ?) AND (${CalendarContract.Events.DESCRIPTION} NOT LIKE ?)"
        val selectionArgs = arrayOf(endTime.toString(), startTime.toString(), "%$FLUIDZ_TAG%")

        return context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun findExistingEventId(context: Context, title: String, startTimeMillis: Long): Long {
        val startWindow = startTimeMillis - (12 * 60 * 60 * 1000)
        val endWindow = startTimeMillis + (12 * 60 * 60 * 1000)

        val projection = arrayOf(CalendarContract.Events._ID, CalendarContract.Events.TITLE)
        val selection = "(${CalendarContract.Events.DESCRIPTION} LIKE ? OR ${CalendarContract.Events.DESCRIPTION} LIKE ?) AND " +
                "(${CalendarContract.Events.DTSTART} >= ?) AND " +
                "(${CalendarContract.Events.DTSTART} <= ?)"
        
        val selectionArgs = arrayOf("%$PENDING_TAG%", "%$VERIFIED_TAG%", startWindow.toString(), endWindow.toString())

        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(CalendarContract.Events._ID)
            val titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE)

            while (cursor.moveToNext()) {
                val existingTitle = cursor.getString(titleIndex)
                // If the titles are similar (e.g. both mention "Dental" or "Doctor")
                if (isTitleMatch(title, existingTitle)) {
                    return cursor.getLong(idIndex)
                }
            }
        }
        return -1L
    }

    private fun isTitleMatch(newTitle: String, existingTitle: String): Boolean {
        val words = newTitle.lowercase().split(" ").filter { it.length > 3 }
        return words.any { existingTitle.lowercase().contains(it) }
    }

    fun getFluidzAppointments(context: Context, type: AppointmentType): List<Appointment> {
        val appointments = mutableListOf<Appointment>()
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DESCRIPTION
            )

            // Filter events that contain our Fluidz tags
            val selection = "(${CalendarContract.Events.DESCRIPTION} LIKE ?) OR (${CalendarContract.Events.DESCRIPTION} LIKE ?)"
            val selectionArgs = arrayOf("%$PENDING_TAG%", "%$VERIFIED_TAG%")

            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

            cursor?.use {
                val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
                val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                val locationIndex = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val descIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)

                while (it.moveToNext()) {
                    val id = it.getString(idIndex)
                    val title = it.getString(titleIndex)
                    val location = it.getString(locationIndex) ?: "No Location"
                    val startTime = it.getLong(startIndex)
                    val description = it.getString(descIndex) ?: ""

                    val isPending = description.contains(PENDING_TAG)
                    val provider = extractProvider(description)
                    val dateStr = sdf.format(Date(startTime))
                    val meetingLink = extractUrl(location) ?: extractUrl(description)

                    val apptType = when {
                        title.contains("Prescription", ignoreCase = true) || description.contains("Pharmacy", ignoreCase = true) -> AppointmentType.PRESCRIPTION
                        title.contains("Appointment", ignoreCase = true) || description.contains("doctor", ignoreCase = true) -> AppointmentType.MEDICAL
                        else -> AppointmentType.EVENT
                    }

                    if (apptType == type) {
                        appointments.add(
                            Appointment(
                                id = id,
                                title = title,
                                location = location,
                                dateTime = dateStr,
                                provider = provider,
                                type = apptType,
                                meetingLink = meetingLink,
                                isPending = isPending,
                                conflictWith = checkForConflict(context, startTime, startTime + 3600000)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Error fetching appointments", e)
        }
        return appointments
    }

    private fun extractUrl(text: String): String? {
        val pattern = java.util.regex.Pattern.compile("https://\\S+")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(0) else null
    }

    fun getDashboardCounts(context: Context): Map<AppointmentType, Int> {
        val counts = mutableMapOf(
            AppointmentType.MEDICAL to 0,
            AppointmentType.EVENT to 0,
            AppointmentType.PRESCRIPTION to 0
        )
        try {
            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION
            )
            val selection = "(${CalendarContract.Events.DESCRIPTION} LIKE ?) OR (${CalendarContract.Events.DESCRIPTION} LIKE ?)"
            val selectionArgs = arrayOf("%$PENDING_TAG%", "%$VERIFIED_TAG%")

            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.use {
                val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)

                while (it.moveToNext()) {
                    val title = it.getString(titleIndex)
                    val description = it.getString(descIndex) ?: ""

                    val apptType = when {
                        title.contains("Prescription", ignoreCase = true) || description.contains("Pharmacy", ignoreCase = true) -> AppointmentType.PRESCRIPTION
                        title.contains("Appointment", ignoreCase = true) || description.contains("doctor", ignoreCase = true) -> AppointmentType.MEDICAL
                        else -> AppointmentType.EVENT
                    }
                    counts[apptType] = counts[apptType]!! + 1
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Error fetching dashboard counts", e)
        }
        return counts
    }

    private fun extractProvider(description: String): String {
        val lines = description.lines()
        for (line in lines) {
            if (line.startsWith("With:", ignoreCase = true)) {
                return line.removePrefix("With:").trim()
            }
        }
        return "N/A"
    }
}
