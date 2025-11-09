package com.cybershield.x.service

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.cybershield.x.CyberShieldApp
import com.cybershield.x.R
import com.cybershield.x.util.SecurePreferences

class SimChangeReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.SIM_STATE_CHANGED") {
            
            checkSimChange(context)
        }
    }
    
    private fun checkSimChange(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        val securePrefs = SecurePreferences(context)
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        try {
            val currentSimSerial = tm.simSerialNumber
            val savedSimSerial = securePrefs.getSimSerial()
            
            if (savedSimSerial == null) {
                // First time, save the SIM serial
                currentSimSerial?.let { securePrefs.saveSimSerial(it) }
            } else if (currentSimSerial != null && currentSimSerial != savedSimSerial) {
                // SIM changed - trigger alert
                showSimChangeAlert(context)
                
                // Update to new SIM
                securePrefs.saveSimSerial(currentSimSerial)
            }
        } catch (e: SecurityException) {
            // Permission denied
        }
    }
    
    private fun showSimChangeAlert(context: Context) {
        val notification = NotificationCompat.Builder(
            context,
            CyberShieldApp.CHANNEL_SIM_ALERT
        )
            .setContentTitle("⚠️ SIM Card Changed")
            .setContentText("A different SIM card has been detected in your device")
            .setSmallIcon(R.drawable.ic_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(3001, notification)
    }
}
