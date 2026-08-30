package com.suprxsidh.onestop.battery.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Insert
    suspend fun insert(reading: Reading): Long

    @Query("SELECT * FROM readings ORDER BY ts DESC LIMIT 1")
    fun latest(): Flow<Reading?>

    @Query("SELECT * FROM readings WHERE ts BETWEEN :startTs AND :endTs ORDER BY ts ASC")
    suspend fun between(startTs: Long, endTs: Long): List<Reading>

    @Query("SELECT * FROM readings ORDER BY ts ASC")
    fun all(): Flow<List<Reading>>
}
