package com.cybershield.x.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cybershield.x.CyberShieldApp
import com.cybershield.x.data.repository.AppRepository
import com.cybershield.x.ui.Screen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import android.content.Intent
import android.provider.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val database = remember { 
        try {
            (context.applicationContext as CyberShieldApp).database
        } catch (e: Exception) {
            null
        }
    }
    val repository = remember { 
        database?.let { AppRepository(context, it) }
    }
    
    val allApps by (repository?.getAllApps() ?: flowOf(emptyList())).collectAsState(initial = emptyList())
    val threatApps by (repository?.getThreatApps() ?: flowOf(emptyList())).collectAsState(initial = emptyList())
    
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CyberShield-X") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Scanner") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Scanner.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text("Privacy") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Privacy.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text("Locker") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Locker.route) }
                )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Security Status",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard("Total Apps", allApps.size.toString())
                            StatCard("Threats", threatApps.size.toString())
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isScanning = true
                            errorMessage = null
                            try {
                                repository?.scanInstalledApps()
                            } catch (e: Exception) {
                                errorMessage = "Scan failed: ${e.message}"
                            } finally {
                                isScanning = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isScanning && repository != null
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isScanning) "Scanning..." else "Scan All Apps")
                }
                
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        title = "URL Scanner",
                        icon = "🔍",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.UrlScanner.route) }
                    )
                    QuickActionCard(
                        title = "Parental",
                        icon = "👨‍👩‍👧",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Parental.route) }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        title = "Remote Wipe",
                        icon = "🗑️",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.RemoteWipe.route) }
                    )
                    QuickActionCard(
                        title = "Settings",
                        icon = "⚙️",
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            try {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Could not open settings
                            }
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Recent Threats",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(threatApps.take(5)) { app ->
                ThreatAppCard(
                    appName = app.appName,
                    packageName = app.packageName,
                    riskScore = app.riskScore
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ThreatAppCard(appName: String, packageName: String, riskScore: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = packageName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = "${(riskScore * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
