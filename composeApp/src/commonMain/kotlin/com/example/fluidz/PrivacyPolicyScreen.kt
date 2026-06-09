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
                    text = "Your Privacy is Our Priority",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                PrivacySection(
                    title = "Absolute Data Security",
                    content = "At Fluidz, we understand that your health and personal information are sacred. All sensitive data—including your email address and monitored senders—is stored using hardware-backed AES-256 encryption. This means your information is mathematically unreadable to anyone but you."
                )

                PrivacySection(
                    title = "No 3rd Party Sharing",
                    content = "We have a zero-compromise policy: Your data will NEVER be sold, rented, or shared with 3rd party companies, advertisers, or any external entities. We are here to serve you, not profit from your information."
                )

                PrivacySection(
                    title = "No Malicious Use",
                    content = "Your information is used strictly for the automation of your healthcare schedule. It will never be used for tracking, profiling, or any form of malicious activity."
                )

                PrivacySection(
                    title = "Zero Active Tracking",
                    content = "Fluidz does not actively track your location or monitor your device behavior. While the app uses system permissions to integrate with your calendar and location services, this data stays entirely on your device and is never transmitted to our servers."
                )

                PrivacySection(
                    title = "For Veterans, By Veterans",
                    content = "This app is built on a foundation of trust and service. We hold ourselves to the highest standards of integrity to protect those who have protected us."
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "100% SECURE. 100% PRIVATE.",
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontSize = 18.sp
                )
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
