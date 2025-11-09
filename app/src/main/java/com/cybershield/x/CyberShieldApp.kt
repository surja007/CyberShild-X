package com.cybershield.x

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cybershield.x.data.local.AppDatabase
import com.cybershield.x.service.ThreatScanWorker
import java.util.concurrent.TimeUnit

class CyberShieldApp : Application() {
    
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        
        database = AppDatabase.getInstance(this)
        createNotificationChannels()
        scheduleThreatScanning()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_THREAT_SCAN,
                    "Threat Scanning",
                    NotificationManager.IMPORTANCE_LOW
                ),
                NotificationChannel(
                    CHANNEL_PRIVACY_ALERT,
                    "Privacy Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    CHANNEL_SIM_ALERT,
                    "SIM Change Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            
            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    private fun scheduleThreatScanning() {
        val scanRequest = PeriodicWorkRequestBuilder<ThreatScanWorker>(
            6, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "threat_scan",
            ExistingPeriodicWorkPolicy.KEEP,
            scanRequest
        )
    }

    companion object {
        const val CHANNEL_THREAT_SCAN = "threat_scan"
        const val CHANNEL_PRIVACY_ALERT = "privacy_alert"
        const val CHANNEL_SIM_ALERT = "sim_alert"
    }
}
