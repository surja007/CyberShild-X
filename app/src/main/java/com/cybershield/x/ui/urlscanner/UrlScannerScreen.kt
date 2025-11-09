package com.cybershield.x.ui.urlscanner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cybershield.x.util.PhishingDetector
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlScannerScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    
    var url by remember { mutableStateOf("") }
    var localResult by remember { mutableStateOf<PhishingDetector.PhishingResult?>(null) }
    var geminiResult by remember { mutableStateOf<com.cybershield.x.util.GeminiAnalyzer.UrlAnalysisResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var useGemini by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("URL Scanner") },
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Phishing Protection",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (useGemini) "AI-Powered Analysis" else "Local Analysis",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = useGemini,
                            onCheckedChange = { 
                                useGemini = it
                                localResult = null
                                geminiResult = null
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Check if a URL is safe before visiting",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = url,
                onValueChange = { 
                    url = it
                    localResult = null
                    geminiResult = null
                    errorMessage = null
                },
                label = { Text("Enter URL") },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        isScanning = true
                        errorMessage = null
                        try {
                            if (useGemini) {
                                geminiResult = com.cybershield.x.util.GeminiAnalyzer.analyzeUrl(url)
                                localResult = null
                            } else {
                                localResult = PhishingDetector.analyzeUrl(url)
                                geminiResult = null
                            }
                        } catch (e: Exception) {
                            errorMessage = "Scan failed: ${e.message}"
                        } finally {
                            isScanning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = url.isNotBlank() && !isScanning
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanning) "Scanning..." else "Scan URL")
            }
            
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Show Gemini result
            geminiResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            result.isPhishing -> MaterialTheme.colorScheme.errorContainer
                            result.confidence > 0.5 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = result.category,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "AI Analysis",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                text = "${(result.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.displaySmall,
                                color = if (result.isPhishing) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analysis:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = result.analysis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (result.indicators.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Threat Indicators:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            result.indicators.forEach { indicator ->
                                Text(
                                    text = "• $indicator",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        if (result.recommendations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Recommendations:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            result.recommendations.forEach { rec ->
                                Text(
                                    text = "• $rec",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        if (result.isPhishing) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "⚠️ DANGER: Do not visit this URL!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Show local result
            localResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            result.isPhishing -> MaterialTheme.colorScheme.errorContainer
                            result.confidence > 0.3 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.category,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${(result.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (result.isPhishing) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (result.reasons.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reasons:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            result.reasons.forEach { reason ->
                                Text(
                                    text = "• $reason",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        if (result.isPhishing) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "⚠️ Do not visit this URL!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
