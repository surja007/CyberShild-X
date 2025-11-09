package com.cybershield.x.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*

object BiometricHelper {
    
    data class BiometricStatus(
        val isAvailable: Boolean,
        val canUseBiometric: Boolean,
        val canUseDeviceCredential: Boolean,
        val statusMessage: String,
        val errorMessage: String? = null,
        val availableMethods: List<String> = emptyList()
    )
    
    fun checkBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val availableMethods = mutableListOf<String>()
        var statusMessage = ""
        var errorMessage: String? = null
        var canUseBiometric = false
        var canUseDeviceCredential = false
        
        // Check for strong biometrics (Fingerprint + Face ID)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                statusMessage = "Biometric authentication ready"
                canUseBiometric = true
                
                // Try to determine which biometric types are available
                if (hasFingerprint(context)) {
                    availableMethods.add("Fingerprint")
                }
                if (hasFaceRecognition(context)) {
                    availableMethods.add("Face ID")
                }
                if (availableMethods.isEmpty()) {
                    availableMethods.add("Biometric")
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                statusMessage = "No biometric hardware available"
                errorMessage = "This device doesn't support biometric authentication"
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                statusMessage = "Biometric hardware unavailable"
                errorMessage = "Biometric sensor is currently unavailable"
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                statusMessage = "No biometrics enrolled"
                errorMessage = "Please enroll fingerprint or face in device settings"
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                statusMessage = "Security update required"
                errorMessage = "Please update your device security settings"
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                statusMessage = "Biometric not supported"
                errorMessage = "This Android version doesn't support biometric authentication"
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                statusMessage = "Biometric status unknown"
                errorMessage = "Unable to determine biometric availability"
            }
        }
        
        // Check for device credential (PIN/Pattern/Password)
        when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                canUseDeviceCredential = true
                availableMethods.add("PIN/Pattern/Password")
            }
        }
        
        val isAvailable = canUseBiometric || canUseDeviceCredential
        
        return BiometricStatus(
            isAvailable = isAvailable,
            canUseBiometric = canUseBiometric,
            canUseDeviceCredential = canUseDeviceCredential,
            statusMessage = statusMessage,
            errorMessage = errorMessage,
            availableMethods = availableMethods
        )
    }
    
    private fun hasFingerprint(context: Context): Boolean {
        return try {
            context.packageManager.hasSystemFeature("android.hardware.fingerprint")
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasFaceRecognition(context: Context): Boolean {
        return try {
            context.packageManager.hasSystemFeature("android.hardware.biometrics.face")
        } catch (e: Exception) {
            false
        }
    }
    
    fun openBiometricSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                )
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to security settings
            try {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Could not open settings
            }
        }
    }
    
    fun getAuthenticatorTypes(): Int {
        return BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    }
}
