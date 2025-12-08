package com.example.conecta4.data.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Insert
    suspend fun insert(stat: GameStat)

    @Query("SELECT * FROM game_stats ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<GameStat>>

    @Query("DELETE FROM game_stats")
    suspend fun clear()
}
