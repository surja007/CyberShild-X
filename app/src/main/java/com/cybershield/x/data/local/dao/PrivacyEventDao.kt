package com.cybershield.x.data.local.dao

import androidx.room.*
import com.cybershield.x.data.local.entities.PrivacyEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacyEventDao {
    @Query("SELECT * FROM privacy_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEvents(): Flow<List<PrivacyEvent>>
    
    @Insert
    suspend fun insertEvent(event: PrivacyEvent)
    
    @Query("DELETE FROM privacy_events WHERE timestamp < :cutoffTime")
    suspend fun deleteOldEvents(cutoffTime: Long)
}
