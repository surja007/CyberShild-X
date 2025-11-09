package com.cybershield.x.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.cybershield.x.data.local.AppDatabase
import com.cybershield.x.data.local.entities.InstalledAppEntity
import com.cybershield.x.data.local.entities.ThreatLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class AppRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    
    private val appDao = database.appDao()
    private val threatLogDao = database.threatLogDao()
    
    fun getAllApps(): Flow<List<InstalledAppEntity>> = appDao.getAllApps()
    
    fun getThreatApps(): Flow<List<InstalledAppEntity>> = appDao.getThreatApps()
    
    fun getAllThreatLogs(): Flow<List<ThreatLog>> = threatLogDao.getAllLogs()
    
    suspend fun scanInstalledApps(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            
            val apps = packages.map { pkg ->
                createAppEntity(pkg, pm)
            }
            
            appDao.insertApps(apps)
            Result.success(apps.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun analyzeApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val app = appDao.getApp(packageName) ?: return@withContext Result.failure(
                Exception("App not found")
            )
            
            // Use local threat analyzer instead of API
            val result = com.cybershield.x.util.LocalThreatAnalyzer.analyzeApp(app)
            
            val updatedApp = app.copy(
                riskScore = result.score,
                threatLabel = result.label,
                lastScanned = System.currentTimeMillis()
            )
            appDao.updateApp(updatedApp)
            
            val log = ThreatLog(
                packageName = app.packageName,
                appName = app.appName,
                riskScore = result.score,
                status = result.label,
                threatType = result.threatType
            )
            threatLogDao.insertLog(log)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun analyzeAppWithGemini(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val app = appDao.getApp(packageName) ?: return@withContext Result.failure(
                Exception("App not found")
            )
            
            // Use Gemini AI for analysis
            val result = com.cybershield.x.util.GeminiAnalyzer.analyzeApp(app)
            
            val updatedApp = app.copy(
                riskScore = result.riskScore,
                threatLabel = result.threatLabel,
                lastScanned = System.currentTimeMillis()
            )
            appDao.updateApp(updatedApp)
            
            val log = ThreatLog(
                packageName = app.packageName,
                appName = app.appName,
                riskScore = result.riskScore,
                status = result.threatLabel,
                threatType = result.threatType
            )
            threatLogDao.insertLog(log)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createAppEntity(pkg: PackageInfo, pm: PackageManager): InstalledAppEntity {
        val appInfo = pkg.applicationInfo
        val appName = appInfo.loadLabel(pm).toString()
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val permissions = pkg.requestedPermissions?.joinToString(", ") ?: "None"
        
        val size = try {
            File(appInfo.sourceDir).length()
        } catch (e: Exception) {
            0L
        }
        
        return InstalledAppEntity(
            packageName = pkg.packageName,
            appName = appName,
            versionName = pkg.versionName,
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkg.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pkg.versionCode.toLong()
            },
            installDate = pkg.firstInstallTime,
            updateDate = pkg.lastUpdateTime,
            size = size,
            isSystemApp = isSystemApp,
            permissions = permissions
        )
    }
}
