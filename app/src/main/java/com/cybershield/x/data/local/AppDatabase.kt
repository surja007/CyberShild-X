package com.cybershield.x.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cybershield.x.data.local.dao.AppDao
import com.cybershield.x.data.local.dao.PrivacyEventDao
import com.cybershield.x.data.local.dao.ThreatLogDao
import com.cybershield.x.data.local.entities.InstalledAppEntity
import com.cybershield.x.data.local.entities.PrivacyEvent
import com.cybershield.x.data.local.entities.ThreatLog
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        InstalledAppEntity::class,
        ThreatLog::class,
        PrivacyEvent::class,
        com.cybershield.x.data.local.entities.LockedApp::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun threatLogDao(): ThreatLogDao
    abstract fun privacyEventDao(): PrivacyEventDao
    abstract fun lockedAppDao(): com.cybershield.x.data.local.dao.LockedAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SQLiteDatabase.getBytes("CyberShield-X-2024".toCharArray())
                val factory = SupportFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cybershield_db"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
