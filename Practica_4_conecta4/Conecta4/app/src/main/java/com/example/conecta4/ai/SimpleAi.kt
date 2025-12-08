package com.example.conecta4.ai

import kotlin.random.Random

object SimpleAi {
    /**
     * Devuelve la columna que la IA va a jugar.
     * Estrategia: ganar si puede, bloquear si el rival puede ganar en el próximo turno,
     * si no, columna central preferida, luego aleatoria válida.
     */
    fun chooseMove(
        board: Array<IntArray>, // 0 vacío, 1 J1, 2 J2
        aiPlayer: Int,
        winCheck: (row: Int, col: Int, who: Int) -> Boolean
    ): Int? {
        val cols = board[0].indices.toList()
        val validCols = cols.filter { board[0][it] == 0 }
        if (validCols.isEmpty()) return null

        val opp = if (aiPlayer == 1) 2 else 1

        fun dropPreview(col: Int, who: Int): Pair<Int, Int>? {
            for (r in board.indices.reversed()) {
                if (board[r][col] == 0) {
                    return r to col
                }
            }
            return null
        }

        // 1) ¿Puedo ganar ahora?
        for (c in validCols) {
            val spot = dropPreview(c, aiPlayer) ?: continue
            board[spot.first][spot.second] = aiPlayer
            val win = winCheck(spot.first, spot.second, aiPlayer)
            board[spot.first][spot.second] = 0
            if (win) return c
        }

        // 2) ¿Bloquear victoria del rival?
        for (c in validCols) {
            val spot = dropPreview(c, opp) ?: continue
            board[spot.first][spot.second] = opp
            val oppWin = winCheck(spot.first, spot.second, opp)
            board[spot.first][spot.second] = 0
            if (oppWin) return c
        }

        // 3) Preferir centro
        val center = board[0].size / 2
        if (center in validCols) return center

        // 4) Aleatoria válida
        return validCols.random(Random)
    }
}
