package com.example.fluidz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
    onReply: (String) -> Unit = {},
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
            OasisBackground(isSecondary = true)
            if (appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No upcoming ${title.lowercase()}.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .widthIn(max = 800.dp)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(appointments) { appointment ->
                        AppointmentItem(
                            appointment = appointment,
                            onApprove = onApprove,
                            onReply = onReply,
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
    onReply: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    val primaryBlue = Color(0xFF003366)
    val burntOrange = Color(0xFFCC5500)
    val draftBgColor = primaryBlue.copy(alpha = 0.15f)
    val verifiedBgColor = Color.White.copy(alpha = 0.9f)
    
    val titleColor = if (appointment.isPending) Color(0xFF333333) else Color.Black
    val detailColor = if (appointment.isPending) Color(0xFF555555) else Color.DarkGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (appointment.isPending) {
                    Modifier.drawBehind {
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = primaryBlue,
                            style = stroke,
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                    }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (appointment.isPending) draftBgColor else verifiedBgColor
        ),
        elevation = if (appointment.isPending) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = appointment.title,
                    fontSize = 18.sp,
                    fontWeight = if (appointment.isPending) FontWeight.Bold else FontWeight.ExtraBold,
                    color = titleColor,
                    modifier = Modifier.weight(1f)
                )
                if (appointment.isPending) {
                    Surface(
                        color = burntOrange,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "DRAFT", 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // High-contrast black on orange
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
                color = if (appointment.isPending) detailColor else burntOrange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: ${appointment.location}",
                fontSize = 14.sp,
                color = detailColor
            )
            Text(
                text = "With: ${appointment.provider}",
                fontSize = 14.sp,
                color = detailColor
            )

            if (appointment.isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            onApprove(appointment.id)
                            onReply(appointment.provider)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                    ) {
                        Text("Approve & Reply", fontSize = 12.sp, color = Color.White)
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
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("JOIN MEETING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
