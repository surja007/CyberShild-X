package com.cybershield.x.ui.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cybershield.x.CyberShieldApp
import com.cybershield.x.data.repository.AppRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(navController: NavController) {
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
    
    val allApps by (repository?.getAllApps() ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())
    var analyzingPackage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var useGemini by remember { mutableStateOf(true) }
    
    val systemApps = allApps.filter { it.isSystemApp }
    val userApps = allApps.filter { !it.isSystemApp }
    val displayApps = when (selectedTab) {
        0 -> allApps
        1 -> userApps
        2 -> systemApps
        else -> allApps
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Scanner") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${allApps.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("User (${userApps.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("System (${systemApps.size})") }
                )
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI-Powered Scanning",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (useGemini) "Using Gemini AI" else "Using Local Analysis",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = useGemini,
                                onCheckedChange = { useGemini = it }
                            )
                        }
                    }
                    
                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            
            items(displayApps) { app ->
                AppScanCard(
                    appName = app.appName,
                    packageName = app.packageName,
                    riskScore = app.riskScore,
                    threatLabel = app.threatLabel,
                    isSystemApp = app.isSystemApp,
                    isAnalyzing = analyzingPackage == app.packageName,
                    useGemini = useGemini,
                    onAnalyze = {
                        scope.launch {
                            analyzingPackage = app.packageName
                            errorMessage = null
                            try {
                                if (useGemini) {
                                    repository?.analyzeAppWithGemini(app.packageName)
                                } else {
                                    repository?.analyzeApp(app.packageName)
                                }
                            } catch (e: Exception) {
                                errorMessage = "Analysis failed: ${e.message}"
                            } finally {
                                analyzingPackage = null
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    }
}

@Composable
fun AppScanCard(
    appName: String,
    packageName: String,
    riskScore: Double,
    threatLabel: String,
    isSystemApp: Boolean,
    isAnalyzing: Boolean,
    useGemini: Boolean,
    onAnalyze: () -> Unit
) {
    val cardColor = when {
        riskScore > 0.7 -> MaterialTheme.colorScheme.errorContainer
        riskScore > 0.4 -> MaterialTheme.colorScheme.tertiaryContainer
        riskScore > 0 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                
                if (riskScore > 0) {
                    Text(
                        text = "${(riskScore * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (riskScore > 0.7) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: $threatLabel",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Button(
                    onClick = onAnalyze,
                    enabled = !isAnalyzing,
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Analyze")
                    }
                }
            }
        }
    }
}
