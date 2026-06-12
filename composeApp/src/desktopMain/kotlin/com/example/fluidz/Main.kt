package com.example.fluidz

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(
        width = 1024.dp,
        height = 768.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Fluidz - For Veterans, By Veterans",
        state = windowState
    ) {
        // Enforce minimum size for accessibility on high-scaling displays
        window.minimumSize = java.awt.Dimension(800, 600)

        val authManager = remember { AuthManager() }
        val dashboardCounts by produceState(initialValue = emptyMap<AppointmentType, Int>()) {
            value = DesktopCalendarManager.getDashboardCounts()
        }
        
        App(
            isDarkMode = false, 
            onAddEvent = { /* Open System Calendar or link */ },
            onAddAppointment = { /* Open System Calendar or link */ },
            onGoogleSignIn = {
                authManager.signInWithGoogle(onSuccess = {}, onError = { println(it) })
            },
            onAppleSignIn = {
                authManager.signInWithApple(onSuccess = {}, onError = { println(it) })
            },
            settingsContent = { onBack ->
                // Basic settings placeholder for Desktop
                androidx.compose.foundation.layout.Box {
                    androidx.compose.material3.Text("Settings coming soon to Desktop")
                    androidx.compose.material3.Button(onClick = onBack) {
                        androidx.compose.material3.Text("Back")
                    }
                }
            },
            appointmentsContent = { title, type, onBack, onReply ->
                val appointments by produceState(initialValue = emptyList<Appointment>(), key1 = type) {
                    value = DesktopCalendarManager.getFluidzAppointments(type)
                }
                AppointmentsScreen(
                    title = title,
                    appointments = appointments,
                    onBackClick = onBack,
                    onApprove = { /* Approve logic */ },
                    onReply = onReply,
                    onEdit = { /* Edit logic */ }
                )
            },
            dashboardCounts = dashboardCounts
        )
    }
}
