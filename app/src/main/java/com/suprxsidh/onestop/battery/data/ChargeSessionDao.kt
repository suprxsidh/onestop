package com.suprxsidh.onestop.battery.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeSessionDao {
    @Insert
    suspend fun insert(session: ChargeSession): Long

    @Update
    suspend fun update(session: ChargeSession)

    @Query("SELECT * FROM charge_sessions ORDER BY startTs DESC")
    fun all(): Flow<List<ChargeSession>>

    @Query("SELECT * FROM charge_sessions WHERE id = :id")
    suspend fun byId(id: Long): ChargeSession?
}
