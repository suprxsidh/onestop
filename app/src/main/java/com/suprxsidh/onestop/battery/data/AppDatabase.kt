package com.suprxsidh.onestop.battery.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Reading::class, ChargeSession::class, HealthEstimate::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao
    abstract fun chargeSessionDao(): ChargeSessionDao
    abstract fun healthEstimateDao(): HealthEstimateDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "onestop.db"
                ).build().also { instance = it }
            }

        fun setTestInstance(db: AppDatabase) {
            instance = db
        }

        fun clearTestInstance() {
            instance = null
        }
    }
}
