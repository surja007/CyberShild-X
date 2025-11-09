package com.cybershield.x.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean = true,
    val lockedAt: Long = System.currentTimeMillis()
)
