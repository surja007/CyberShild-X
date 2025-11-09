package com.cybershield.x.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val versionName: String?,
    val versionCode: Long,
    val installDate: Long,
    val updateDate: Long,
    val size: Long,
    val isSystemApp: Boolean,
    val permissions: String,
    val riskScore: Double = 0.0,
    val threatLabel: String = "Unknown",
    val lastScanned: Long = System.currentTimeMillis()
)
