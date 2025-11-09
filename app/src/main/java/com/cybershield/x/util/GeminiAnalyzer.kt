package com.cybershield.x.util

import com.cybershield.x.data.local.entities.InstalledAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiAnalyzer {
    
    private const val API_KEY = "AIzaSyAIZjHAaozWjHCtv4-pPV2aHvB0y-SJjjo"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
    
    data class GeminiResult(
        val isSuccess: Boolean,
        val riskScore: Double,
        val threatLabel: String,
        val threatType: String,
        val analysis: String,
        val recommendations: List<String> = emptyList()
    )
    
    data class UrlAnalysisResult(
        val isSuccess: Boolean,
        val isPhishing: Boolean,
        val confidence: Double,
        val category: String,
        val analysis: String,
        val indicators: List<String> = emptyList(),
        val recommendations: List<String> = emptyList()
    )
    
    suspend fun analyzeApp(app: InstalledAppEntity): GeminiResult = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(app)
            val response = callGeminiAPI(prompt)
            parseGeminiResponse(response)
        } catch (e: Exception) {
            // Fallback to local analysis if API fails
            val localResult = LocalThreatAnalyzer.analyzeApp(app)
            GeminiResult(
                isSuccess = false,
                riskScore = localResult.score,
                threatLabel = localResult.label,
                threatType = localResult.threatType,
                analysis = "Using local analysis (API unavailable)",
                recommendations = localResult.reasons
            )
        }
    }
    
    private fun buildPrompt(app: InstalledAppEntity): String {
        return """
            Analyze this Android app for security threats and privacy risks:
            
            App Name: ${app.appName}
            Package Name: ${app.packageName}
            Is System App: ${app.isSystemApp}
            Permissions: ${app.permissions}
            Version: ${app.versionName} (${app.versionCode})
            Size: ${app.size / 1024 / 1024} MB
            Install Date: ${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(app.installDate))}
            
            Provide a security analysis in this exact JSON format:
            {
                "riskScore": 0.0-1.0,
                "threatLabel": "Safe|Suspicious|Malicious",
                "threatType": "None|Privacy Risk|Malware|Adware|Spyware|Data Exfiltration",
                "analysis": "Brief security analysis",
                "recommendations": ["recommendation1", "recommendation2"]
            }
            
            Consider:
            1. Permission combinations that indicate malicious behavior
            2. Known malware patterns in package names
            3. Excessive permissions for app type
            4. Privacy risks from data collection
            5. System app vs user app context
            
            Respond ONLY with valid JSON, no additional text.
        """.trimIndent()
    }
    
    private suspend fun callGeminiAPI(prompt: String): String = withContext(Dispatchers.IO) {
        val url = URL("$API_URL?key=$API_KEY")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            val requestBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 1024)
                })
            }
            
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("API Error: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    private fun parseGeminiResponse(response: String): GeminiResult {
        try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            
            // Extract JSON from response (remove markdown code blocks if present)
            val jsonText = text
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val result = JSONObject(jsonText)
            
            val recommendations = mutableListOf<String>()
            val recsArray = result.optJSONArray("recommendations")
            if (recsArray != null) {
                for (i in 0 until recsArray.length()) {
                    recommendations.add(recsArray.getString(i))
                }
            }
            
            return GeminiResult(
                isSuccess = true,
                riskScore = result.getDouble("riskScore"),
                threatLabel = result.getString("threatLabel"),
                threatType = result.getString("threatType"),
                analysis = result.getString("analysis"),
                recommendations = recommendations
            )
        } catch (e: Exception) {
            throw Exception("Failed to parse Gemini response: ${e.message}")
        }
    }
    
    suspend fun analyzeUrl(url: String): UrlAnalysisResult = withContext(Dispatchers.IO) {
        try {
            val prompt = buildUrlPrompt(url)
            val response = callGeminiAPI(prompt)
            parseUrlResponse(response)
        } catch (e: Exception) {
            // Fallback to local analysis if API fails
            val localResult = PhishingDetector.analyzeUrl(url)
            UrlAnalysisResult(
                isSuccess = false,
                isPhishing = localResult.isPhishing,
                confidence = localResult.confidence,
                category = localResult.category,
                analysis = "Using local analysis (API unavailable)",
                indicators = localResult.reasons,
                recommendations = if (localResult.isPhishing) 
                    listOf("Do not visit this URL", "Report as phishing") 
                else 
                    listOf("URL appears safe")
            )
        }
    }
    
    private fun buildUrlPrompt(url: String): String {
        return """
            Analyze this URL for phishing, malware, and security threats:
            
            URL: $url
            
            Provide a comprehensive security analysis in this exact JSON format:
            {
                "isPhishing": true/false,
                "confidence": 0.0-1.0,
                "category": "Safe|Suspicious|Phishing|Malware|Scam",
                "analysis": "Detailed analysis of the URL",
                "indicators": ["indicator1", "indicator2"],
                "recommendations": ["recommendation1", "recommendation2"]
            }
            
            Consider:
            1. Domain reputation and age
            2. SSL/TLS certificate validity
            3. URL structure and patterns (IP addresses, excessive subdomains, suspicious TLDs)
            4. Known phishing keywords and brand impersonation
            5. Suspicious URL shorteners
            6. Homograph attacks (lookalike domains)
            7. Malicious redirects
            8. Known malware distribution sites
            9. Typosquatting attempts
            10. Social engineering indicators
            
            Respond ONLY with valid JSON, no additional text.
        """.trimIndent()
    }
    
    private fun parseUrlResponse(response: String): UrlAnalysisResult {
        try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            
            // Extract JSON from response
            val jsonText = text
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val result = JSONObject(jsonText)
            
            val indicators = mutableListOf<String>()
            val indicatorsArray = result.optJSONArray("indicators")
            if (indicatorsArray != null) {
                for (i in 0 until indicatorsArray.length()) {
                    indicators.add(indicatorsArray.getString(i))
                }
            }
            
            val recommendations = mutableListOf<String>()
            val recsArray = result.optJSONArray("recommendations")
            if (recsArray != null) {
                for (i in 0 until recsArray.length()) {
                    recommendations.add(recsArray.getString(i))
                }
            }
            
            return UrlAnalysisResult(
                isSuccess = true,
                isPhishing = result.getBoolean("isPhishing"),
                confidence = result.getDouble("confidence"),
                category = result.getString("category"),
                analysis = result.getString("analysis"),
                indicators = indicators,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            throw Exception("Failed to parse URL analysis response: ${e.message}")
        }
    }
}
