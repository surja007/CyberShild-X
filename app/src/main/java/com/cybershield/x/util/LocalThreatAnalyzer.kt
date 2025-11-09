package com.cybershield.x.util

import com.cybershield.x.data.local.entities.InstalledAppEntity

object LocalThreatAnalyzer {
    
    private val dangerousPermissions = setOf(
        "READ_CONTACTS",
        "READ_SMS",
        "SEND_SMS",
        "READ_CALL_LOG",
        "WRITE_CALL_LOG",
        "CAMERA",
        "RECORD_AUDIO",
        "ACCESS_FINE_LOCATION",
        "ACCESS_COARSE_LOCATION",
        "READ_PHONE_STATE",
        "CALL_PHONE",
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "WRITE_SETTINGS",
        "SYSTEM_ALERT_WINDOW",
        "REQUEST_INSTALL_PACKAGES",
        "BIND_DEVICE_ADMIN"
    )
    
    private val suspiciousKeywords = setOf(
        "hack", "crack", "mod", "cheat", "spy", "stealth", 
        "hidden", "tracker", "monitor", "keylog", "root", "xposed",
        "lucky", "parallel", "clone", "fake", "vpn"
    )
    
    private val bannedAppPatterns = setOf(
        "com.malware", "com.trojan", "com.spyware", "com.adware"
    )
    
    fun analyzeApp(app: InstalledAppEntity): ThreatResult {
        var riskScore = 0.0
        val reasons = mutableListOf<String>()
        
        // Check permissions
        val permissions = app.permissions.split(", ")
        val dangerousCount = permissions.count { it in dangerousPermissions }
        
        when {
            dangerousCount > 8 -> {
                riskScore += 0.4
                reasons.add("Excessive permissions ($dangerousCount)")
            }
            dangerousCount > 5 -> {
                riskScore += 0.25
                reasons.add("Many permissions ($dangerousCount)")
            }
            dangerousCount > 3 -> {
                riskScore += 0.15
                reasons.add("Several permissions ($dangerousCount)")
            }
        }
        
        // Check for suspicious app name
        val appNameLower = app.appName.lowercase()
        val packageNameLower = app.packageName.lowercase()
        
        if (suspiciousKeywords.any { appNameLower.contains(it) || packageNameLower.contains(it) }) {
            riskScore += 0.3
            reasons.add("Suspicious name")
        }
        
        // Check if non-system app with system-level permissions
        if (!app.isSystemApp && dangerousCount > 6) {
            riskScore += 0.2
            reasons.add("Non-system app with high permissions")
        }
        
        // Check for internet + sensitive permissions combo
        val hasInternet = permissions.contains("INTERNET")
        val hasSensitiveData = permissions.any { 
            it in setOf("READ_CONTACTS", "READ_SMS", "READ_CALL_LOG", "ACCESS_FINE_LOCATION")
        }
        
        if (hasInternet && hasSensitiveData) {
            riskScore += 0.15
            reasons.add("Internet + sensitive data access")
        }
        
        // Cap at 1.0
        riskScore = minOf(riskScore, 1.0)
        
        val label = when {
            riskScore > 0.7 -> "Malicious"
            riskScore > 0.4 -> "Suspicious"
            else -> "Safe"
        }
        
        val threatType = when {
            riskScore > 0.7 -> determineThreatType(permissions, reasons)
            else -> "None"
        }
        
        return ThreatResult(
            label = label,
            score = riskScore,
            threatType = threatType,
            reasons = reasons
        )
    }
    
    private fun determineThreatType(permissions: List<String>, reasons: List<String>): String {
        return when {
            permissions.any { it in setOf("READ_SMS", "SEND_SMS", "READ_CALL_LOG") } -> 
                "Privacy Risk"
            permissions.contains("INTERNET") && permissions.size > 10 -> 
                "Data Exfiltration"
            reasons.any { it.contains("Suspicious") } -> 
                "Potentially Unwanted"
            else -> 
                "Suspicious Behavior"
        }
    }
    
    data class ThreatResult(
        val label: String,
        val score: Double,
        val threatType: String,
        val reasons: List<String>
    )
}
