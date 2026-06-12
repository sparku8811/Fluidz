package com.example.fluidz

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fluidz.ui.theme.FluidzTheme
import fluidz.composeapp.generated.resources.Res
import fluidz.composeapp.generated.resources.ic_fluidz_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    isDarkMode: Boolean,
    userEmail: String = "veteran@example.com",
    accountTier: String = "Fluidz Beta Member",
    onAddEvent: () -> Unit = {},
    onAddAppointment: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {},
    settingsContent: @Composable (onBack: () -> Unit) -> Unit,
    appointmentsContent: @Composable (title: String, type: AppointmentType, onBack: () -> Unit, onReply: (String) -> Unit) -> Unit,
    dashboardCounts: Map<AppointmentType, Int> = emptyMap(),
) {
    var currentScreen by remember { mutableStateOf("start") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    FluidzTheme(darkTheme = isDarkMode) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = currentScreen != "start" && currentScreen != "signup",
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(0.85f),
                    drawerContainerColor = Color.White
                ) {
                    // Drawer Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF003366))
                            .padding(24.dp)
                    ) {
                        Column {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = userEmail,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = accountTier,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Menu Items - Grouped by priority
                    NavigationDrawerItem(
                        label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                        selected = currentScreen == "main",
                        onClick = {
                            currentScreen = "main"
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = currentScreen == "settings",
                        onClick = {
                            currentScreen = "settings"
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Help & Support") },
                        selected = currentScreen == "help",
                        onClick = {
                            currentScreen = "help"
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("About Fluidz") },
                        selected = currentScreen == "about",
                        onClick = {
                            currentScreen = "about"
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    NavigationDrawerItem(
                        label = { Text("Privacy Policy", fontSize = 12.sp) },
                        selected = currentScreen == "privacy",
                        onClick = {
                            currentScreen = "privacy"
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        ) {
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
                        counts = dashboardCounts,
                        onOpenMenu = { scope.launch { drawerState.open() } }
                    )
                    "settings" -> settingsContent { currentScreen = "main" }
                    "about" -> AboutScreen { currentScreen = "main" }
                    "privacy" -> PrivacyPolicyScreen { currentScreen = "main" }
                    "help" -> HelpScreen { currentScreen = "main" }
                    "upcoming_appointments" -> appointmentsContent("Upcoming Appointments", AppointmentType.MEDICAL, { currentScreen = "main" }) { _ -> /* MainActivity logic */ }
                    "upcoming_events" -> appointmentsContent("Upcoming Events", AppointmentType.EVENT, { currentScreen = "main" }) { _ -> /* MainActivity logic */ }
                    "upcoming_prescriptions" -> appointmentsContent("Prescription Pickups", AppointmentType.PRESCRIPTION, { currentScreen = "main" }) { _ -> /* MainActivity logic */ }
                    "signup" -> SignUpScreen(
                        onBackClick = { currentScreen = "start" },
                        onGoogleSignIn = onGoogleSignIn,
                        onAppleSignIn = onAppleSignIn,
                    )
                }
            }
        }
    }
}

@Composable
fun StartScreen(onStartClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FluidBackground()
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_fluidz_logo),
                contentDescription = "Fluidz Logo",
                modifier = Modifier.sizeIn(maxWidth = 200.dp, maxHeight = 200.dp).fillMaxWidth(0.5f).aspectRatio(1f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fluidz",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartClick,
                modifier = Modifier.widthIn(max = 300.dp).fillMaxWidth(0.8f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC5500))
            ) {
                Text(
                    text = "START", 
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "100% For Veterans, 100% Made By Veterans",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: (String) -> Unit,
    onAddEvent: () -> Unit,
    onAddAppointment: () -> Unit,
    counts: Map<AppointmentType, Int>,
    onOpenMenu: () -> Unit
) {
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
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(
                            Icons.Default.Menu, 
                            contentDescription = "Menu",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            OasisBackground()
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .widthIn(max = 800.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome to your Oasis",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold, // Bolded Phrase
                    color = Color.Black // Black Lettering
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Dashboard Stacks
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardCard(
                        title = "Appointments",
                        count = counts[AppointmentType.MEDICAL] ?: 0,
                        icon = Icons.Default.MedicalServices,
                        color = Color(0xFF003366), // Dark Blue
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate("upcoming_appointments") }
                    
                    DashboardCard(
                        title = "Events",
                        count = counts[AppointmentType.EVENT] ?: 0,
                        icon = Icons.Default.Event,
                        color = Color(0xFFCC5500), // Burnt Orange
                        textColor = Color.Black,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate("upcoming_events") }
                    
                    DashboardCard(
                        title = "Prescriptions",
                        count = counts[AppointmentType.PRESCRIPTION] ?: 0,
                        icon = Icons.Default.Vaccines,
                        color = Color(0xFF555555), // Gray
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) { onNavigate("upcoming_prescriptions") }
                }

                Spacer(modifier = Modifier.height(32.dp))
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
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigate("upcoming_prescriptions") },
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)) // Gray
                ) {
                    Text(
                        "VIEW PRESCRIPTION PICKUPS",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = title.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            OasisBackground(isSecondary = true)
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .widthIn(max = 600.dp)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
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
