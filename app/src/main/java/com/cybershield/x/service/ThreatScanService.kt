package com.cybershield.x.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cybershield.x.CyberShieldApp
import com.cybershield.x.R

class ThreatScanService : Service() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CyberShieldApp.CHANNEL_THREAT_SCAN)
            .setContentTitle("CyberShield-X Active")
            .setContentText("Real-time protection enabled")
            .setSmallIcon(R.drawable.ic_shield)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(SERVICE_ID, notification)
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        private const val SERVICE_ID = 2001
    }
}
