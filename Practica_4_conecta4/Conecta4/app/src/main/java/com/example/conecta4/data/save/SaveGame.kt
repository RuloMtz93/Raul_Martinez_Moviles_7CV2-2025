package com.example.conecta4.data.save

import com.example.conecta4.Cell
import com.example.conecta4.GameMode

/**
 * Modelo portable de guardado. No usa Compose State.
 */
data class SaveGame(
    val cols: Int,
    val rows: Int,
    val board: List<List<Cell>>,
    val currentPlayer: Cell,
    val scoreP1: Int,
    val scoreP2: Int,
    val draws: Int,
    val gameOver: Boolean,
    val winner: Cell?,
    val mode: GameMode,
    val isMyTurn: Boolean,
    val winningCells: List<Pair<Int, Int>>,

    // NUEVO
    val moves: Int,
    val startedAt: Long,
    val elapsedMs: Long = 0L,              // tiempo acumulado (si partida sigue, lo que lleva)
    val moveHistory: List<Int> = emptyList(), // historial de columnas jugadas
    val tag: String? = null                // etiqueta/categoría opcional
)
