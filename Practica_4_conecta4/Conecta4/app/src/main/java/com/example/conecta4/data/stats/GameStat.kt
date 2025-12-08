package com.example.conecta4.data.stats

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.conecta4.GameMode

@Entity(tableName = "game_stats")
data class GameStat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val mode: GameMode,
    val result: String,          // "P1", "P2" o "DRAW"
    val moves: Int,
    val durationMs: Long
)
