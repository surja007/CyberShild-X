package com.cybershield.x.ui.locker

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.cybershield.x.data.local.entities.LockedApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockerScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val database = remember { (context.applicationContext as com.cybershield.x.CyberShieldApp).database }
    val allApps by database.appDao().getAllApps().collectAsState(initial = emptyList())
    val lockedApps by database.lockedAppDao().getLockedApps().collectAsState(initial = emptyList())
    
    var showAppSelector by remember { mutableStateOf(false) }
    var isServiceEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isServiceEnabled = isAccessibilityServiceEnabled(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Locker") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isServiceEnabled) {
                FloatingActionButton(onClick = { showAppSelector = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add app")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                if (!isServiceEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ Service Not Enabled",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enable Accessibility Service to lock apps",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { openAccessibilitySettings(context) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enable Service")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Locked Apps",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "${lockedApps.size} apps protected",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (lockedApps.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔓",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No apps locked yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tap + to add apps to protect",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            items(lockedApps.size) { index ->
                val app = lockedApps[index]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                scope.launch {
                                    database.lockedAppDao().unlockApp(app.packageName)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Unlock"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    
    if (showAppSelector) {
        AppSelectorDialog(
            apps = allApps.filter { app ->
                !lockedApps.any { it.packageName == app.packageName }
            },
            onDismiss = { showAppSelector = false },
            onAppSelected = { app ->
                scope.launch {
                    database.lockedAppDao().insertLockedApp(
                        LockedApp(
                            packageName = app.packageName,
                            appName = app.appName
                        )
                    )
                    showAppSelector = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorDialog(
    apps: List<com.cybershield.x.data.local.entities.InstalledAppEntity>,
    onDismiss: () -> Unit,
    onAppSelected: (com.cybershield.x.data.local.entities.InstalledAppEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App to Lock") },
        text = {
            LazyColumn {
                items(apps.size) { index ->
                    val app = apps[index]
                    TextButton(
                        onClick = { onAppSelected(app) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = app.appName,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/com.cybershield.x.service.AppLockService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(service) == true
}

fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}
