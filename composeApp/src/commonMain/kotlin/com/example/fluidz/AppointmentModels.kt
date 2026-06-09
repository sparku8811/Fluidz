package com.example.fluidz

data class Appointment(
    val id: String,
    val title: String,
    val location: String,
    val dateTime: String,
    val provider: String,
    val type: AppointmentType
)

enum class AppointmentType {
    MEDICAL, EVENT
}
