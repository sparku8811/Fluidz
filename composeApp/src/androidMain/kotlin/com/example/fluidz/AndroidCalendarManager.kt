package com.example.fluidz

import android.content.Context
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.*

object AndroidCalendarManager {

    fun getFluidzAppointments(context: Context, type: AppointmentType): List<Appointment> {
        val appointments = mutableListOf<Appointment>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DESCRIPTION
        )

        // Filter events that contain our "Automatically imported by Fluidz" tag
        val selection = "${CalendarContract.Events.DESCRIPTION} LIKE ?"
        val selectionArgs = arrayOf("%Automatically imported by Fluidz%")

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

                val provider = extractProvider(description)
                val dateStr = sdf.format(Date(startTime))

                // Determine if it's medical or event based on keywords or tags in description
                // For now, we can use a simple check or just allow all for both for the beta
                val apptType = if (title.contains("Appointment", ignoreCase = true) || description.contains("doctor", ignoreCase = true)) {
                    AppointmentType.MEDICAL
                } else {
                    AppointmentType.EVENT
                }

                if (apptType == type) {
                    appointments.add(
                        Appointment(id, title, location, dateStr, provider, apptType)
                    )
                }
            }
        }
        return appointments
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
