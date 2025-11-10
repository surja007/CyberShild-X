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
    private var currentUnlockedApp: String? = null
    private val temporaryUnlocks = mutableMapOf<String, Long>()
    private var isLockScreenShowing = false
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore our own package and system UI
            if (packageName == this.packageName || 
                packageName == "com.android.systemui") {
                return
            }
            
            // If user switched away from unlocked app, re-lock it
            if (currentUnlockedApp != null && 
                currentUnlockedApp != packageName && 
                lastPackageName == currentUnlockedApp) {
                // User left the unlocked app, remove from unlocked list
                temporaryUnlocks.remove(currentUnlockedApp)
                currentUnlockedApp = null
            }
            
            lastPackageName = packageName
            
            // Check if app is locked
            scope.launch {
                try {
                    val database = (applicationContext as CyberShieldApp).database
                    val isLocked = database.lockedAppDao().isAppLocked(packageName)
                    
                    if (isLocked == true) {
                        // Check if temporarily unlocked and not expired
                        val unlockTime = temporaryUnlocks[packageName]
                        val isTemporarilyUnlocked = unlockTime != null && 
                            (System.currentTimeMillis() - unlockTime) < UNLOCK_DURATION
                        
                        if (!isTemporarilyUnlocked && !isLockScreenShowing) {
                            // Remove expired unlock
                            temporaryUnlocks.remove(packageName)
                            
                            // Show lock screen
                            isLockScreenShowing = true
                            val intent = Intent(this@AppLockService, LockScreenActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                                putExtra("PACKAGE_NAME", packageName)
                            }
                            startActivity(intent)
                            
                            // Reset flag after a delay
                            scope.launch {
                                kotlinx.coroutines.delay(1000)
                                isLockScreenShowing = false
                            }
                        }
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
        temporaryUnlocks[packageName] = System.currentTimeMillis()
        currentUnlockedApp = packageName
        
        // Clean up old unlocks
        scope.launch {
            kotlinx.coroutines.delay(UNLOCK_DURATION)
            if (currentUnlockedApp != packageName) {
                temporaryUnlocks.remove(packageName)
            }
        }
    }
    
    companion object {
        private var instance: AppLockService? = null
        private const val UNLOCK_DURATION = 2 * 60 * 1000L // 2 minutes
        
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
