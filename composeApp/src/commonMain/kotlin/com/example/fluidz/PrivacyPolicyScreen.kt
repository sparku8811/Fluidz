package com.example.fluidz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.ExtraBold, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            OasisBackground(isSecondary = true)
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Privacy Policy",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = "Last Updated: June 10, 2026",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Fluidz is built on a foundation of trust and service. We operate under a Zero-Cloud, Zero-Sharing policy. All sensitive data is handled locally on your device.",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PrivacySection(
                    title = "1. Our Privacy Pledge",
                    content = "Your health and schedule information are sacred. Your information is never transmitted to our servers or any third-party entities. We process everything right where it belongs: on your device."
                )

                PrivacySection(
                    title = "2. Information Collection & Use",
                    content = "• Email & SMS: We monitor official VA notifications to extract appointment details via local AI.\n• Privacy Cleansing: Non-scheduling messages (like 2FA codes or spam) are permanently dropped.\n• Calendar: Appointments are placed in a 'Shadow Calendar' and only moved to your primary calendar with your explicit approval."
                )

                PrivacySection(
                    title = "3. Military-Grade Security",
                    content = "All sensitive data is protected using hardware-backed AES-256 encryption. Access is guarded by your device's native biometric authentication (Fingerprint, Face ID, or PIN)."
                )

                PrivacySection(
                    title = "4. Zero Third-Party Sharing",
                    content = "We do not sell, rent, or share your personal information with advertisers or data brokers. Fluidz does not use tracking pixels or behavior-profiling algorithms."
                )

                PrivacySection(
                    title = "5. Multi-Device Sync",
                    content = "Device syncing is handled via private, randomized Sync Keys. This process is anonymous and does not require an account or email address."
                )

                PrivacySection(
                    title = "6. Contact Us",
                    content = "If you have questions about your data security, please contact the Fluidz development team through our official feedback channels."
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "100% SECURE. 100% PRIVATE.",
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title, 
            fontWeight = FontWeight.ExtraBold, 
            color = Color.Black,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content, 
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}
