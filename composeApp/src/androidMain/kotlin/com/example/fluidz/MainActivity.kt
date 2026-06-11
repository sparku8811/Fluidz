package com.example.fluidz

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private var isAppLocked by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = isSystemInDarkTheme()

            if (isAppLocked) {
                LockScreen { 
                    isAppLocked = false 
                    AppLockManager.resetLock()
                }
            } else {
                val authManager = remember { AuthManager(this@MainActivity) }
                val dashboardCounts by produceState(initialValue = emptyMap<AppointmentType, Int>(), key1 = isAppLocked) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        value = AndroidCalendarManager.getDashboardCounts(this@MainActivity)
                    }
                }
                App(
                    isDarkMode = isDarkMode,
                    onAddEvent = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "New VA Event")
                        }
                        startActivity(intent)
                    },
                    onAddAppointment = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "New VA Appointment")
                        }
                        startActivity(intent)
                    },
                    onGoogleSignIn = {
                        authManager.signInWithGoogle(
                            onSuccess = { Toast.makeText(this@MainActivity, "Google Success", Toast.LENGTH_SHORT).show() },
                        ) { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() }
                    },
                    onAppleSignIn = {
                        authManager.signInWithApple(
                            onSuccess = { Toast.makeText(this@MainActivity, "Apple Success", Toast.LENGTH_SHORT).show() },
                        ) { Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show() }
                    },
                    settingsContent = { onBack ->
                        SettingsScreen(
                            onBackClick = onBack
                        )
                    },
                    appointmentsContent = { title, type, onBack ->
                        var refreshTrigger by remember { mutableStateOf(0) }
                        val appointments by produceState(initialValue = emptyList<Appointment>(), key1 = type, key2 = refreshTrigger) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                value = AndroidCalendarManager.getFluidzAppointments(this@MainActivity, type)
                            }
                        }
                        AppointmentsScreen(
                            title = title,
                            appointments = appointments,
                            onBackClick = onBack,
                            onApprove = { id ->
                                if (AndroidCalendarManager.approveFluidzEvent(this@MainActivity, id)) {
                                    Toast.makeText(this@MainActivity, "Event Approved!", Toast.LENGTH_SHORT).show()
                                    refreshTrigger++
                                }
                            },
                            onReply = { provider ->
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("smsto:") // Default to SMS
                                    putExtra("sms_body", "Got it! Added to my calendar, see you then.")
                                }
                                startActivity(intent)
                            },
                            onEdit = { id ->
                                val intent = Intent(Intent.ACTION_EDIT).apply {
                                    data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id.toLong())
                                }
                                startActivity(intent)
                            }
                        )
                    },
                    dashboardCounts = dashboardCounts
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (AppLockManager.isLockRequired()) {
            isAppLocked = true
        }
    }

    override fun onPause() {
        super.onPause()
        AppLockManager.updateActivity()
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Auth Error: $errorCode $errString", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fluidz Security")
            .setSubtitle("Unlock using your fingerprint or PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    @Composable
    fun LockScreen(onUnlock: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OasisBackground(isSecondary = true)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Lock, 
                    contentDescription = "Locked", 
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("App Locked", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showBiometricPrompt(onUnlock) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5500)),
                ) {
                    Text("Unlock Fluidz", color = Color.White)
                }
            }
        }
    }
}
