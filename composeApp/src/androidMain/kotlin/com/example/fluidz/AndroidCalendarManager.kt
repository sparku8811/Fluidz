package com.example.fluidz

import android.content.Context
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.*

object AndroidCalendarManager {

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

                    val apptType = when {
                        title.contains("Prescription", ignoreCase = true) || description.contains("Pharmacy", ignoreCase = true) -> AppointmentType.PRESCRIPTION
                        title.contains("Appointment", ignoreCase = true) || description.contains("doctor", ignoreCase = true) -> AppointmentType.MEDICAL
                        else -> AppointmentType.EVENT
                    }

                    if (apptType == type) {
                        appointments.add(
                            Appointment(id, title, location, dateStr, provider, apptType)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Error fetching appointments", e)
        }
        return appointments
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
            val selection = "${CalendarContract.Events.DESCRIPTION} LIKE ?"
            val selectionArgs = arrayOf("%Automatically imported by Fluidz%")

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
