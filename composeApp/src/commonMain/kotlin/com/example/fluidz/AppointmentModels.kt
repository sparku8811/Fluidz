package com.example.fluidz

data class Appointment(
    val id: String,
    val title: String,
    val location: String,
    val dateTime: String,
    val provider: String,
    val type: AppointmentType,
    val meetingLink: String? = null,
    val isPending: Boolean = false,
    val conflictWith: String? = null
)

enum class AppointmentType {
    MEDICAL, EVENT, PRESCRIPTION
}
