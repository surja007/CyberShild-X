package com.cybershield.x.data.local.dao

import androidx.room.*
import com.cybershield.x.data.local.entities.ThreatLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatLogDao {
    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<ThreatLog>>
    
    @Query("SELECT * FROM threat_logs WHERE status = 'Malicious' ORDER BY timestamp DESC")
    fun getMaliciousLogs(): Flow<List<ThreatLog>>
    
    @Insert
    suspend fun insertLog(log: ThreatLog)
    
    @Query("DELETE FROM threat_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}
