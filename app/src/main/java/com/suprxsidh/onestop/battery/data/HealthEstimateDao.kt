package com.suprxsidh.onestop.battery.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthEstimateDao {
    @Insert
    suspend fun insert(estimate: HealthEstimate): Long

    @Query("SELECT * FROM health_estimates ORDER BY ts DESC LIMIT 1")
    fun latest(): Flow<HealthEstimate?>

    @Query("SELECT * FROM health_estimates ORDER BY ts ASC")
    suspend fun all(): List<HealthEstimate>
}
