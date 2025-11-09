package com.cybershield.x.data.local.dao

import androidx.room.*
import com.cybershield.x.data.local.entities.LockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps WHERE isLocked = 1")
    fun getLockedApps(): Flow<List<LockedApp>>
    
    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName")
    suspend fun getLockedApp(packageName: String): LockedApp?
    
    @Query("SELECT isLocked FROM locked_apps WHERE packageName = :packageName")
    suspend fun isAppLocked(packageName: String): Boolean?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockedApp(app: LockedApp)
    
    @Delete
    suspend fun deleteLockedApp(app: LockedApp)
    
    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun unlockApp(packageName: String)
    
    @Query("DELETE FROM locked_apps")
    suspend fun unlockAllApps()
}
