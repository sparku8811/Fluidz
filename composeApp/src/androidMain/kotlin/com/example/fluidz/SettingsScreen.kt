package com.example.fluidz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val sharedPrefs = remember { SecurityUtils.getEncryptedSharedPreferences(context) }
    
    var notificationsEnabled by remember { mutableStateOf(value = true) }
    var pushNotificationsEnabled by remember { mutableStateOf(value = true) }
    var autoCaptureEnabled by remember { mutableStateOf(value = false) }
    var autoCaptureSmsEnabled by remember { mutableStateOf(value = false) }
    var cloudSyncEnabled by remember { mutableStateOf(value = true) }
    var userEmail by remember { 
        mutableStateOf(sharedPrefs.getString("user_email", "") ?: "") 
    }
    
    var customSenders by remember {
        mutableStateOf(sharedPrefs.getStringSet("custom_senders", emptySet()) ?: emptySet())
    }
    var newSenderEmail by remember { mutableStateOf("") }

    var syncedDevices by remember {
        mutableStateOf(DeviceSyncManager.getSyncedDevices(context))
    }

    var locationServiceEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var contactsEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var phoneEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var calendarEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var smsEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationServiceEnabled = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        contactsEnabled = isGranted
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        phoneEnabled = isGranted
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        calendarEnabled = (permissions[Manifest.permission.READ_CALENDAR] == true) &&
                (permissions[Manifest.permission.WRITE_CALENDAR] == true)
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        smsEnabled = (permissions[Manifest.permission.RECEIVE_SMS] == true) &&
                (permissions[Manifest.permission.READ_SMS] == true)
    }

    val accentColor = Color(0xFFCC5500) // Burnt Orange

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Permissions", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                SettingItem(
                    title = "Location Services",
                    checked = locationServiceEnabled,
                ) { checked ->
                    if (checked) {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    } else {
                        locationServiceEnabled = false
                    }
                }
                SettingItem(
                    title = "Access Contacts",
                    checked = contactsEnabled,
                ) { checked ->
                    if (checked) contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    else contactsEnabled = false
                }
                SettingItem(
                    title = "Phone Access",
                    checked = phoneEnabled,
                ) { checked ->
                    if (checked) phonePermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    else phoneEnabled = false
                }
                SettingItem(
                    title = "Calendar Access",
                    checked = calendarEnabled,
                ) { checked ->
                    if (checked) {
                        calendarPermissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                        )
                    } else {
                        calendarEnabled = false
                    }
                }
                SettingItem(
                    title = "SMS Access",
                    checked = smsEnabled,
                ) { checked ->
                    if (checked) {
                        smsPermissionLauncher.launch(
                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                        )
                    } else {
                        smsEnabled = false
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.2f))
                Text(
                    "Account Settings", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { email ->
                        userEmail = email 
                        sharedPrefs.edit { putString("user_email", email) }
                    },
                    label = { Text("Your Email Address") },
                    placeholder = { Text("e.g. yourname@outlook.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Text(
                    text = "Enter your email address where you receive appointment notifications.",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.2f))
                Text(
                    stringResource(R.string.synced_devices), 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sync up to 5 devices to your account for shared alerts.",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Button(
                    onClick = {
                        val currentDevice = "${DeviceSyncManager.getDeviceName()} (${DeviceSyncManager.getDeviceId(context).take(8)})"
                        if (DeviceSyncManager.addDevice(context, currentDevice)) {
                            syncedDevices = DeviceSyncManager.getSyncedDevices(context)
                        } else {
                            Toast.makeText(context, context.getString(R.string.device_limit_reached), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    enabled = syncedDevices.size < 5,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_current_device), color = Color.White)
                }

                syncedDevices.forEach { device ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(device, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, color = Color.Black)
                        IconButton(
                            onClick = {
                                DeviceSyncManager.removeDevice(context, device)
                                syncedDevices = DeviceSyncManager.getSyncedDevices(context)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.2f))
                Text(
                    "Monitored Senders", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "The app automatically monitors official VA senders. Add other trusted senders below.",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSenderEmail,
                        onValueChange = { email -> newSenderEmail = email },
                        label = { Text("Add Sender Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    IconButton(
                        onClick = {
                            if (newSenderEmail.isNotEmpty()) {
                                val updated = customSenders + newSenderEmail
                                customSenders = updated
                                sharedPrefs.edit { putStringSet("custom_senders", updated) }
                                newSenderEmail = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                    }
                }

                customSenders.forEach { sender ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sender, modifier = Modifier.padding(start = 8.dp), color = Color.Black)
                        IconButton(
                            onClick = {
                                val updated = customSenders - sender
                                customSenders = updated
                                sharedPrefs.edit { putStringSet("custom_senders", updated) }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.2f))
                Text(
                    "Notifications & Sync", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                SettingItem(
                    title = "General Notifications",
                    checked = notificationsEnabled,
                ) { checked -> notificationsEnabled = checked }
                SettingItem(
                    title = "Push Notifications",
                    checked = pushNotificationsEnabled,
                ) { checked -> pushNotificationsEnabled = checked }
                SettingItem(
                    title = stringResource(R.string.sync_alerts_title),
                    checked = cloudSyncEnabled,
                ) { checked -> cloudSyncEnabled = checked }
                SettingItem(
                    title = stringResource(R.string.auto_capture_mail),
                    checked = autoCaptureEnabled,
                ) { enabled ->
                    autoCaptureEnabled = enabled
                    if (enabled) {
                        val syncRequest = PeriodicWorkRequestBuilder<EmailSyncWorker>(15, TimeUnit.MINUTES).build()
                        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                            "EmailSync",
                            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                            syncRequest
                        )
                    } else {
                        WorkManager.getInstance(context).cancelUniqueWork("EmailSync")
                    }
                }
                SettingItem(
                    title = stringResource(R.string.auto_capture_sms),
                    checked = autoCaptureSmsEnabled,
                ) { enabled ->
                    autoCaptureSmsEnabled = enabled
                    sharedPrefs.edit { putBoolean("auto_capture_sms", enabled) }
                    if (enabled && !smsEnabled) {
                        smsPermissionLauncher.launch(
                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                        )
                    }
                }
                if (autoCaptureEnabled && userEmail.isNotEmpty()) {
                    Text(
                        text = "Syncing from: $userEmail",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.capturing_from),
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.2f))
                Text(
                    "Communications", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Open Email App", color = Color.White)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MESSAGING)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Open Messaging App", color = Color.White)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.2f))
                Text(
                    "System", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "App appearance is currently mirroring your device's system settings.",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("Device Settings (App Info)")
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFCC5500),
                checkedTrackColor = Color(0xFFE67E22).copy(alpha = 0.5f),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
}
