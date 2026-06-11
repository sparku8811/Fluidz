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
    appointments: List<Appointment>,
    onBackClick: () -> Unit,
    onApprove: (String) -> Unit = {},
    onEdit: (String) -> Unit = {}
) {
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
            if (appointments.isEmpty()) {
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
                    items(appointments) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            onApprove = onApprove,
                            onEdit = onEdit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: Appointment,
    onApprove: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    val alpha = if (appointment.isPending) 0.6f else 1.0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = alpha * 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = appointment.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                if (appointment.isPending) {
                    Surface(
                        color = Color(0xFFCC5500),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "DRAFT", 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (appointment.conflictWith != null) {
                Text(
                    "⚠️ Conflict with: ${appointment.conflictWith}",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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

            if (appointment.isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onApprove(appointment.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366))
                    ) {
                        Text("Approve", fontSize = 12.sp, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = { onEdit(appointment.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit", fontSize = 12.sp, color = Color.Black)
                    }
                }
            } else if (!appointment.meetingLink.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                Button(
                    onClick = { uriHandler.openUri(appointment.meetingLink) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("JOIN MEETING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
