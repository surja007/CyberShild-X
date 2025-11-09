package com.cybershield.x.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_logs")
data class ThreatLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val riskScore: Double,
    val status: String,
    val threatType: String,
    val timestamp: Long = System.currentTimeMillis()
)
