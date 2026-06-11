package com.example.fluidz

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.remember

fun MainViewController(): UIViewController = ComposeUIViewController {
    val authManager = remember { AuthManager() }
    App(
        isDarkMode = false, // You'd pass actual iOS dark mode state here
        onAddEvent = { /* Open iOS Calendar */ },
        onAddAppointment = { /* Open iOS Calendar */ },
        onGoogleSignIn = {
            authManager.signInWithGoogle(onSuccess = {}, onError = {})
        },
        onAppleSignIn = {
            authManager.signInWithApple(onSuccess = {}, onError = {})
        },
        settingsContent = { onBack ->
            // On iOS, you'd implement a different Settings screen 
            // or a common one with expect/actual permissions
            Box {
                Text("Settings Not Implemented for iOS yet")
                Button(onClick = onBack) { Text("Back") }
            }
        },
        appointmentsContent = { title, _, onBack ->
            AppointmentsScreen(
                title = title,
                appointments = emptyList(), // Real iOS calendar sync later
                onBackClick = onBack
            )
        },
        dashboardCounts = emptyMap()
    )
}
