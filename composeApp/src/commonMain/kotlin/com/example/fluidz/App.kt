package com.example.fluidz

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fluidz.ui.theme.FluidzTheme
import fluidz.composeapp.generated.resources.Res
import fluidz.composeapp.generated.resources.ic_fluidz_logo
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    isDarkMode: Boolean,
    onAddEvent: () -> Unit = {},
    onAddAppointment: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {},
    settingsContent: @Composable (onBack: () -> Unit) -> Unit,
) {
    var currentScreen by remember { mutableStateOf("start") }

    FluidzTheme(darkTheme = isDarkMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (currentScreen) {
                "start" -> StartScreen { currentScreen = "main" }
                "main" -> MainScreen(
                    onNavigate = { screen -> currentScreen = screen },
                    onAddEvent = onAddEvent,
                    onAddAppointment = onAddAppointment,
                )
                "settings" -> settingsContent { currentScreen = "main" }
                "about" -> AboutScreen { currentScreen = "main" }
                "privacy" -> PrivacyPolicyScreen { currentScreen = "main" }
                "help" -> HelpScreen { currentScreen = "main" }
                "upcoming_appointments" -> AppointmentsScreen(
                    title = "Upcoming Appointments",
                    type = AppointmentType.MEDICAL,
                    onBackClick = { currentScreen = "main" }
                )
                "upcoming_events" -> AppointmentsScreen(
                    title = "Upcoming Events",
                    type = AppointmentType.EVENT,
                    onBackClick = { currentScreen = "main" }
                )
                "signup" -> SignUpScreen(
                    onBackClick = { currentScreen = "start" },
                    onGoogleSignIn = onGoogleSignIn,
                    onAppleSignIn = onAppleSignIn,
                )
            }
        }
    }
}

@Composable
fun StartScreen(onStartClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        FluidBackground()
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_fluidz_logo),
                contentDescription = "Fluidz Logo",
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fluidz",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold, // Bolded Subject
                color = Color.Black // Black Lettering
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5500)) // Burnt Orange
            ) {
                Text(
                    text = "START", 
                    fontWeight = FontWeight.ExtraBold, // Bolded Subject
                    color = Color.Black // Black Lettering
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "100% For Veterans, 100% Made By Veterans",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    onAddEvent: () -> Unit,
    onAddAppointment: () -> Unit
) {
    var showMenu by remember { mutableStateOf(value = false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.ic_fluidz_logo),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "FLUIDZ", 
                            fontWeight = FontWeight.ExtraBold, // Bolded Subject
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black // Black Lettering
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "Menu",
                            tint = Color.Black // Black Icon
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.Black) },
                            onClick = { 
                                showMenu = false
                                onNavigate("settings")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About", fontWeight = FontWeight.Bold, color = Color.Black) },
                            onClick = { 
                                showMenu = false
                                onNavigate("about")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = Color.Black) },
                            onClick = { 
                                showMenu = false
                                onNavigate("privacy")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Help", fontWeight = FontWeight.Bold, color = Color.Black) },
                            onClick = { 
                                showMenu = false
                                onNavigate("help")
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            OasisBackground()
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to your Oasis",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold, // Bolded Phrase
                    color = Color.Black // Black Lettering
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onAddEvent,
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5500)) // Burnt Orange
                ) {
                    Text(
                        "ADD EVENT", 
                        fontWeight = FontWeight.ExtraBold, // Bolded Subject
                        color = Color.Black // Black Lettering
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddAppointment,
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366)) // Dark Blue
                ) {
                    Text(
                        "ADD APPOINTMENT", 
                        fontWeight = FontWeight.ExtraBold, // Bolded Subject
                        color = Color.Black // Black Lettering
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigate("upcoming_appointments") },
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)) // Gray
                ) {
                    Text(
                        "VIEW UPCOMING APPOINTMENTS", 
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigate("upcoming_events") },
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)) // Gray
                ) {
                    Text(
                        "VIEW UPCOMING EVENTS", 
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Account", 
                        fontWeight = FontWeight.ExtraBold, // Bolded Subject
                        color = Color.Black 
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black
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
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { 
                        Text(
                            "Email Address", 
                            fontWeight = FontWeight.Bold, 
                            color = Color.Black
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCC5500),
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { 
                        Text(
                            "Password", 
                            fontWeight = FontWeight.Bold, 
                            color = Color.Black
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFCC5500),
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Handle Sign Up Logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5500))
                ) {
                    Text(
                        "Sign Up", 
                        color = Color.Black, // Black Lettering
                        fontWeight = FontWeight.ExtraBold // Bolded Subject
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.5f))
                    Text(
                        " OR ", 
                        color = Color.Black, 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Black.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onGoogleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text(
                        "Sign in with Google",
                        fontWeight = FontWeight.Bold // Bolded Phrase
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onAppleSignIn,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text(
                        "Sign in with Apple",
                        fontWeight = FontWeight.Bold // Bolded Phrase
                    )
                }

                TextButton(onClick = onBackClick) {
                    Text(
                        "Back to Start", 
                        color = Color.Black,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
