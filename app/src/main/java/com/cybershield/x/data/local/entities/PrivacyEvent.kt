package com.cybershield.x.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_events")
data class PrivacyEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val eventType: String, // CAMERA, MICROPHONE, LOCATION
    val timestamp: Long = System.currentTimeMillis()
)
