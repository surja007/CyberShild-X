package com.cybershield.x.data.local.dao

import androidx.room.*
import com.cybershield.x.data.local.entities.InstalledAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM installed_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<InstalledAppEntity>>
    
    @Query("SELECT * FROM installed_apps WHERE riskScore > 0.7 ORDER BY riskScore DESC")
    fun getThreatApps(): Flow<List<InstalledAppEntity>>
    
    @Query("SELECT * FROM installed_apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): InstalledAppEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: InstalledAppEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<InstalledAppEntity>)
    
    @Update
    suspend fun updateApp(app: InstalledAppEntity)
    
    @Query("DELETE FROM installed_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)
    
    @Query("DELETE FROM installed_apps")
    suspend fun deleteAll()
}
