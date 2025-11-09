package com.cybershield.x.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.cybershield.x.CyberShieldApp
import com.cybershield.x.ui.lockscreen.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppLockService : AccessibilityService() {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastPackageName: String? = null
    private val unlockedApps = mutableSetOf<String>()
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore our own package and system UI
            if (packageName == this.packageName || 
                packageName == "com.android.systemui" ||
                packageName == lastPackageName) {
                return
            }
            
            lastPackageName = packageName
            
            // Check if app is locked
            scope.launch {
                try {
                    val database = (applicationContext as CyberShieldApp).database
                    val isLocked = database.lockedAppDao().isAppLocked(packageName)
                    
                    if (isLocked == true && !unlockedApps.contains(packageName)) {
                        // Show lock screen
                        val intent = Intent(this@AppLockService, LockScreenActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("PACKAGE_NAME", packageName)
                        }
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override fun onInterrupt() {
        // Service interrupted
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service connected
    }
    
    fun unlockApp(packageName: String) {
        unlockedApps.add(packageName)
        // Auto-lock after 5 minutes
        scope.launch {
            kotlinx.coroutines.delay(5 * 60 * 1000)
            unlockedApps.remove(packageName)
        }
    }
    
    companion object {
        private var instance: AppLockService? = null
        
        fun getInstance(): AppLockService? = instance
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
