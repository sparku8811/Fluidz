package com.example.fluidz

object DesktopCalendarManager {
    fun getFluidzAppointments(type: AppointmentType): List<Appointment> {
        // Desktop calendar integration (e.g. Outlook/System) can be implemented here
        val _type = type // Use parameter to suppress warning
        return emptyList()
    }

    fun getDashboardCounts(): Map<AppointmentType, Int> {
        return mapOf(
            AppointmentType.MEDICAL to 0,
            AppointmentType.EVENT to 0,
            AppointmentType.PRESCRIPTION to 0
        )
    }
}
