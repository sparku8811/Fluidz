package com.example.fluidz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AppointmentsScreen(
    title: String,
    type: AppointmentType,
    onBackClick: () -> Unit
) {
    // Mock data for now - will be replaced with real data from Calendar/DB later
    val mockAppointments = listOf(
        Appointment("1", "Dental Exam", "VA Medical Center", "Oct 12, 2026 - 10:30 AM", "Dr. Smith", AppointmentType.MEDICAL),
        Appointment("2", "Physical Therapy", "Westside Clinic", "Oct 15, 2026 - 2:00 PM", "Jane Doe", AppointmentType.MEDICAL),
        Appointment("3", "Veteran Meetup", "Community Hall", "Oct 20, 2026 - 6:00 PM", "VFW Post 123", AppointmentType.EVENT),
    ).filter { it.type == type }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.ExtraBold, color = Color.Black) },
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
            if (mockAppointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No upcoming ${title.lowercase()}.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mockAppointments) { appointment ->
                        AppointmentItem(appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = appointment.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = appointment.dateTime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCC5500) // Burnt Orange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: ${appointment.location}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Text(
                text = "With: ${appointment.provider}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}
