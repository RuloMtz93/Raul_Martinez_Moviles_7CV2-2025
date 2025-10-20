package com.example.conecta4

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.max
import kotlin.math.min

// Representación de los jugadores y celdas
enum class Cell { EMPTY, P1, P2 }

data class Score(
    val p1Wins: Int = 0,
    val p2Wins: Int = 0,
    val draws: Int = 0
)

data class GameUiState(
    val cols: Int = 7,
    val rows: Int = 6,
    val board: List<List<Cell>> = List(6) { List(7) { Cell.EMPTY } },
    val currentPlayer: Cell = Cell.P1,
    val gameOver: Boolean = false,
    val winner: Cell? = null,                 // null: nadie; P1/P2: ganador
    val winningCells: Set<Pair<Int, Int>> = emptySet(), // coordenadas que ganaron
    val score: Score = Score(),
    val message: String = "Turno: Jugador 1 (Rojo)"
)

class GameViewModel : ViewModel() {

    var state by mutableStateOf(GameUiState())
        private set

    // Intenta colocar una pieza en la columna 'c'
    fun playColumn(c: Int) {
        if (state.gameOver) return
        if (c !in 0 until state.cols) return

        // Buscar la fila más baja disponible en esa columna
        val r = findAvailableRow(c) ?: return // Columna llena -> movimiento inválido

        // Actualizar el tablero
        val newBoard = state.board.map { it.toMutableList() }.toMutableList()
        newBoard[r][c] = state.currentPlayer

        // Verificar victoria o empate
        val (isWin, winningSet) = checkWin(newBoard, r, c)
        val isDraw = !isWin && newBoard.all { row -> row.none { it == Cell.EMPTY } }

        // Actualizar puntajes y estado final si aplica
        var newScore = state.score
        var msg = if (state.currentPlayer == Cell.P1) "Turno: Jugador 2 (Amarillo)" else "Turno: Jugador 1 (Rojo)"
        var winner: Cell? = null
        var gameOver = false

        if (isWin) {
            winner = state.currentPlayer
            gameOver = true
            msg = if (winner == Cell.P1) "¡Gana Jugador 1 (Rojo)!" else "¡Gana Jugador 2 (Amarillo)!"
            newScore = if (winner == Cell.P1)
                newScore.copy(p1Wins = newScore.p1Wins + 1)
            else
                newScore.copy(p2Wins = newScore.p2Wins + 1)
        } else if (isDraw) {
            gameOver = true
            msg = "Empate"
            newScore = newScore.copy(draws = newScore.draws + 1)
        }

        state = state.copy(
            board = newBoard.map { it.toList() },
            currentPlayer = if (!gameOver) nextPlayer(state.currentPlayer) else state.currentPlayer,
            gameOver = gameOver,
            winner = winner,
            winningCells = if (isWin) winningSet else emptySet(),
            score = newScore,
            message = msg
        )
    }

    fun resetRound() {
        // Reinicia solo el tablero y el flujo de turno (alternamos quién empieza para variedad)
        val starter = if ((state.score.p1Wins + state.score.p2Wins + state.score.draws) % 2 == 0) Cell.P1 else Cell.P2
        state = state.copy(
            board = List(state.rows) { List(state.cols) { Cell.EMPTY } },
            currentPlayer = starter,
            gameOver = false,
            winner = null,
            winningCells = emptySet(),
            message = if (starter == Cell.P1) "Turno: Jugador 1 (Rojo)" else "Turno: Jugador 2 (Amarillo)"
        )
    }

    fun resetAll() {
        // Reinicia tablero y marcador
        state = GameUiState()
    }

    private fun nextPlayer(p: Cell) = if (p == Cell.P1) Cell.P2 else Cell.P1

    private fun findAvailableRow(c: Int): Int? {
        // Desde la última fila hacia arriba (efecto "gravedad")
        for (r in state.rows - 1 downTo 0) {
            if (state.board[r][c] == Cell.EMPTY) return r
        }
        return null
    }

    // Revisa si colocar en (r, c) produjo 4 en línea
    private fun checkWin(board: List<List<Cell>>, r: Int, c: Int): Pair<Boolean, Set<Pair<Int, Int>>> {
        val player = board[r][c]
        if (player == Cell.EMPTY) return false to emptySet()

        val directions = listOf(
            1 to 0,  // horizontal
            0 to 1,  // vertical
            1 to 1,  // diagonal ↘
            1 to -1  // diagonal ↗
        )

        for ((dx, dy) in directions) {
            val cells = collectLine(board, r, c, dx, dy, player)
            if (cells.size >= 4) return true to cells.take(4).toSet() // resalta 4 celdas
        }
        return false to emptySet()
    }

    // Junta celdas consecutivas iguales en una dirección y su opuesta (cuenta total)
    private fun collectLine(
        board: List<List<Cell>>,
        r: Int,
        c: Int,
        dx: Int,
        dy: Int,
        player: Cell
    ): List<Pair<Int, Int>> {
        val cols = board[0].size
        val rows = board.size

        fun inside(x: Int, y: Int) = x in 0 until cols && y in 0 until rows

        val acc = mutableListOf<Pair<Int, Int>>()

        // Hacia -dx, -dy
        var x = c
        var y = r
        while (inside(x, y) && board[y][x] == player) {
            x -= dx; y -= dy
        }
        // Avanzamos una para estar nuevamente en la primera válida
        x += dx; y += dy

        // Recorremos hacia +dx, +dy acumulando consecutivas
        while (inside(x, y) && board[y][x] == player) {
            acc.add(y to x) // guardamos como (row, col)
            x += dx; y += dy
        }

        // Ordenamos para que se muestren contiguas
        return acc
    }
}
