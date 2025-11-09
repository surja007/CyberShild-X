package com.cybershield.x.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cybershield.x.CyberShieldApp
import com.cybershield.x.R
import com.cybershield.x.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreatScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val database = (context.applicationContext as CyberShieldApp).database
    private val repository = AppRepository(context, database)
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            showScanningNotification()
            
            // Scan all installed apps
            val scanResult = repository.scanInstalledApps()
            
            if (scanResult.isSuccess) {
                val appCount = scanResult.getOrNull() ?: 0
                showCompletionNotification(appCount)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showScanningNotification() {
        val notification = NotificationCompat.Builder(
            applicationContext,
            CyberShieldApp.CHANNEL_THREAT_SCAN
        )
            .setContentTitle("CyberShield-X")
            .setContentText("Scanning installed apps...")
            .setSmallIcon(R.drawable.ic_shield)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showCompletionNotification(appCount: Int) {
        val notification = NotificationCompat.Builder(
            applicationContext,
            CyberShieldApp.CHANNEL_THREAT_SCAN
        )
            .setContentTitle("Scan Complete")
            .setContentText("Scanned $appCount apps")
            .setSmallIcon(R.drawable.ic_shield)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
