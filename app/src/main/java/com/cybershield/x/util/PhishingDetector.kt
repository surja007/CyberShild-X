package com.cybershield.x.util

object PhishingDetector {
    
    private val suspiciousTLDs = setOf(
        ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".work"
    )
    
    private val phishingKeywords = setOf(
        "verify", "account", "suspended", "confirm", "update", "secure",
        "login", "signin", "banking", "paypal", "amazon", "apple",
        "microsoft", "google", "facebook", "instagram", "whatsapp"
    )
    
    private val suspiciousPatterns = listOf(
        Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), // IP address
        Regex("@"), // @ symbol in URL
        Regex("-{2,}"), // Multiple dashes
        Regex("\\d{5,}") // Long number sequences
    )
    
    fun analyzeUrl(url: String): PhishingResult {
        var riskScore = 0.0
        val reasons = mutableListOf<String>()
        
        val urlLower = url.lowercase()
        
        // Check for suspicious TLDs
        if (suspiciousTLDs.any { urlLower.endsWith(it) }) {
            riskScore += 0.4
            reasons.add("Suspicious domain extension")
        }
        
        // Check for phishing keywords
        val keywordCount = phishingKeywords.count { urlLower.contains(it) }
        if (keywordCount > 0) {
            riskScore += 0.2 * keywordCount
            reasons.add("Contains $keywordCount phishing keywords")
        }
        
        // Check for suspicious patterns
        suspiciousPatterns.forEach { pattern ->
            if (pattern.containsMatchIn(url)) {
                riskScore += 0.15
                reasons.add("Suspicious URL pattern detected")
            }
        }
        
        // Check URL length
        if (url.length > 100) {
            riskScore += 0.1
            reasons.add("Unusually long URL")
        }
        
        // Check for HTTPS
        if (!url.startsWith("https://")) {
            riskScore += 0.2
            reasons.add("Not using secure HTTPS")
        }
        
        // Check for subdomain count
        val subdomainCount = url.split(".").size - 2
        if (subdomainCount > 3) {
            riskScore += 0.15
            reasons.add("Too many subdomains")
        }
        
        riskScore = minOf(riskScore, 1.0)
        
        val isPhishing = riskScore > 0.6
        val category = when {
            riskScore > 0.8 -> "High Risk - Likely Phishing"
            riskScore > 0.6 -> "Medium Risk - Suspicious"
            riskScore > 0.3 -> "Low Risk - Caution Advised"
            else -> "Safe"
        }
        
        return PhishingResult(
            isPhishing = isPhishing,
            confidence = riskScore,
            category = category,
            reasons = reasons
        )
    }
    
    data class PhishingResult(
        val isPhishing: Boolean,
        val confidence: Double,
        val category: String,
        val reasons: List<String>
    )
}
