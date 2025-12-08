package com.example.conecta4

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.conecta4.bluetooth.BluetoothGameService
import com.example.conecta4.data.save.SaveFormat
import com.example.conecta4.data.save.SaveGame
import com.example.conecta4.data.save.SaveManager
import com.example.conecta4.data.stats.GameStat
import com.example.conecta4.data.stats.StatsRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

// ----------------- MODELOS -----------------

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
    val winner: Cell? = null,
    val winningCells: Set<Pair<Int, Int>> = emptySet(),
    val score: Score = Score(),
    val message: String = "Turno: Jugador 1 (Rojo)",
    val mode: GameMode = GameMode.LOCAL,
    val isMyTurn: Boolean = true,

    // Métricas
    val moves: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),

    // NUEVO: persistimos en saves
    val elapsedMs: Long = 0L,
    val moveHistory: List<Int> = emptyList(),
    val tag: String? = null
)

// ----------------- VIEWMODEL -----------------

class GameViewModel(app: Application) : AndroidViewModel(app) {

    // para Compose:
    var state = androidx.compose.runtime.mutableStateOf(GameUiState())
        private set

    private var aiJob: Job? = null
    private var btJob: Job? = null

    // Cambia de modo (LOCAL / AI / BT) y prepara listeners
    fun setMode(mode: GameMode, myTurnFirst: Boolean = true) {
        val st = state.value
        state.value = st.copy(mode = mode, isMyTurn = myTurnFirst)

        if (mode == GameMode.BT) {
            startBluetoothListener()
        } else {
            btJob?.cancel(); btJob = null
        }

        val now = state.value
        if (mode == GameMode.AI && now.currentPlayer == Cell.P2 && !now.gameOver) {
            scheduleAiMove()
        }
    }

    private fun startBluetoothListener() {
        btJob?.cancel()
        btJob = viewModelScope.launch {
            BluetoothGameService.incomingMoves.collect { col ->
                playColumn(col, fromRemote = true)
            }
        }
    }

    private fun scheduleAiMove() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            delay(350)
            val col = chooseAiColumn()
            if (col != null) playColumn(col)
        }
    }

    // ----------------- IA (mejorada) -----------------

    private fun chooseAiColumn(): Int? {
        val st = state.value
        val colsRange = 0 until st.cols
        val validCols = colsRange.filter { firstFreeRow(st.board, it) != null }
        if (validCols.isEmpty()) return null

        val me = Cell.P2
        val opp = Cell.P1
        val center = st.cols / 2

        // 1) ganar ahora
        for (c in validCols) {
            val tmp = deepCopyBoard(st.board)
            val r = firstFreeRow(tmp, c)!!
            tmp[r][c] = me
            val (win, _) = checkWin(tmp, r, c)
            if (win) return c
        }

        // 2) bloquear rival
        for (c in validCols) {
            val tmp = deepCopyBoard(st.board)
            val r = firstFreeRow(tmp, c)!!
            tmp[r][c] = opp
            val (oppWin, _) = checkWin(tmp, r, c)
            if (oppWin) return c
        }

        // 3) heurística
        var bestScore = Int.MIN_VALUE
        val bestCandidates = mutableListOf<Int>()

        for (c in validCols) {
            val tmp = deepCopyBoard(st.board)
            val r = firstFreeRow(tmp, c)!!
            tmp[r][c] = me

            if (opponentHasImmediateWin(tmp, opp, st.cols)) {
                val score = -10_000 + centerProximityScore(c, center, st.cols)
                if (score > bestScore) {
                    bestScore = score
                    bestCandidates.clear()
                    bestCandidates.add(c)
                } else if (score == bestScore) {
                    bestCandidates.add(c)
                }
                continue
            }

            val myLongest = longestChain(tmp, r, c, me)
            val centerScore = centerProximityScore(c, center, st.cols)
            val oppThreats = countOpponentImmediateWins(tmp, opp, st.cols)

            var score = myLongest * 100 + centerScore * 10
            score -= oppThreats * 5
            score += Random.nextInt(0, 5)

            if (score > bestScore) {
                bestScore = score
                bestCandidates.clear()
                bestCandidates.add(c)
            } else if (score == bestScore) {
                bestCandidates.add(c)
            }
        }

        return if (bestCandidates.isNotEmpty()) bestCandidates.random() else validCols.random()
    }

    private fun centerProximityScore(c: Int, center: Int, totalCols: Int): Int =
        totalCols - abs(c - center)

    private fun opponentHasImmediateWin(boardAfterMyMove: List<List<Cell>>, opp: Cell, totalCols: Int): Boolean {
        for (c in 0 until totalCols) {
            val r = firstFreeRow(boardAfterMyMove, c) ?: continue
            val tmp = deepCopyBoard(boardAfterMyMove)
            tmp[r][c] = opp
            val (win, _) = checkWin(tmp, r, c)
            if (win) return true
        }
        return false
    }

    private fun countOpponentImmediateWins(board: List<List<Cell>>, opp: Cell, totalCols: Int): Int {
        var count = 0
        for (c in 0 until totalCols) {
            val r = firstFreeRow(board, c) ?: continue
            val tmp = deepCopyBoard(board)
            tmp[r][c] = opp
            val (win, _) = checkWin(tmp, r, c)
            if (win) count++
        }
        return count
    }

    private fun longestChain(board: List<List<Cell>>, r: Int, c: Int, player: Cell): Int {
        val dirs = listOf(
            1 to 0,  // horizontal
            0 to 1,  // vertical
            1 to 1,  // diagonal ↘
            1 to -1  // diagonal ↗
        )
        var best = 1
        for ((dx, dy) in dirs) {
            val a = countSame(board, r, c, dx, dy, player)
            val b = countSame(board, r, c, -dx, -dy, player)
            val total = a + b + 1
            if (total > best) best = total
        }
        return best
    }

    private fun countSame(
        board: List<List<Cell>>,
        r: Int, c: Int,
        dx: Int, dy: Int,
        player: Cell
    ): Int {
        val rows = board.size
        val cols = board[0].size
        var x = c + dx
        var y = r + dy
        var cnt = 0
        while (x in 0 until cols && y in 0 until rows && board[y][x] == player) {
            cnt += 1
            x += dx
            y += dy
        }
        return cnt
    }

    // ----------------- JUEGO / ESTADO -----------------

    fun playColumn(c: Int, fromRemote: Boolean = false) {
        val st = state.value

        if (st.gameOver) return
        if (c !in 0 until st.cols) return
        if (st.mode == GameMode.BT && !fromRemote && !st.isMyTurn) return

        val r = findAvailableRow(st.board, st.rows, c) ?: return

        val newBoard = st.board.map { it.toMutableList() }.toMutableList()
        newBoard[r][c] = st.currentPlayer

        val (isWin, winningSet) = checkWin(newBoard, r, c)
        val isDraw = !isWin && newBoard.all { row -> row.none { it == Cell.EMPTY } }

        var newScore = st.score
        var msg = if (st.currentPlayer == Cell.P1) "Turno: Jugador 2 (Amarillo)" else "Turno: Jugador 1 (Rojo)"
        var winner: Cell? = null
        var gameOver = false

        if (isWin) {
            winner = st.currentPlayer
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

        val nextPlayer = if (!gameOver) nextPlayer(st.currentPlayer) else st.currentPlayer
        val nextIsMyTurn = when {
            st.mode != GameMode.BT -> st.isMyTurn
            !fromRemote -> !st.isMyTurn
            else -> !st.isMyTurn
        }

        val nowMs = System.currentTimeMillis()
        val newElapsed = nowMs - st.startedAt
        val finishedNow = gameOver

        state.value = st.copy(
            board = newBoard.map { it.toList() },
            currentPlayer = nextPlayer,
            gameOver = gameOver,
            winner = winner,
            winningCells = if (isWin) winningSet else emptySet(),
            score = newScore,
            message = msg,
            isMyTurn = nextIsMyTurn,
            moves = st.moves + 1,
            moveHistory = st.moveHistory + c,
            elapsedMs = newElapsed
        )

        if (state.value.mode == GameMode.BT && !fromRemote) {
            viewModelScope.launch { BluetoothGameService.sendMove(c) }
        }

        // Registrar estadística al finalizar
        if (finishedNow) {
            val result = when {
                winner == Cell.P1 -> "P1"
                winner == Cell.P2 -> "P2"
                else -> "DRAW"
            }
            viewModelScope.launch {
                StatsRepo.insert(
                    GameStat(
                        timestamp = nowMs,
                        mode = state.value.mode,
                        result = result,
                        moves = state.value.moves,
                        durationMs = state.value.elapsedMs
                    )
                )
            }
        }

        val cur = state.value
        if (cur.mode == GameMode.AI && !cur.gameOver && cur.currentPlayer == Cell.P2) {
            scheduleAiMove()
        }
    }

    fun resetRound() {
        val st = state.value
        val starter = if ((st.score.p1Wins + st.score.p2Wins + st.score.draws) % 2 == 0) Cell.P1 else Cell.P2
        state.value = st.copy(
            board = List(st.rows) { List(st.cols) { Cell.EMPTY } },
            currentPlayer = starter,
            gameOver = false,
            winner = null,
            winningCells = emptySet(),
            message = if (starter == Cell.P1) "Turno: Jugador 1 (Rojo)" else "Turno: Jugador 2 (Amarillo)",
            isMyTurn = st.isMyTurn,
            moves = 0,
            startedAt = System.currentTimeMillis(),
            elapsedMs = 0L,
            moveHistory = emptyList()
        )
        val now = state.value
        if (now.mode == GameMode.AI && now.currentPlayer == Cell.P2) {
            scheduleAiMove()
        }
    }

    fun resetAll() {
        state.value = GameUiState()
    }

    override fun onCleared() {
        super.onCleared()
        aiJob?.cancel()
        btJob?.cancel()
    }

    // ----------------- SAVE / LOAD -----------------

    fun saveGame(format: SaveFormat): String {
        val data = toSaveGame(state.value)
        val file = SaveManager.save(getApplication(), data, format)
        return file.absolutePath
    }

    fun loadGame(format: SaveFormat): Boolean {
        val loaded = SaveManager.load(getApplication(), format) ?: return false
        applySaveGame(loaded)
        return true
    }

    private fun toSaveGame(st: GameUiState): SaveGame =
        SaveGame(
            cols = st.cols,
            rows = st.rows,
            board = st.board,
            currentPlayer = st.currentPlayer,
            scoreP1 = st.score.p1Wins,
            scoreP2 = st.score.p2Wins,
            draws = st.score.draws,
            gameOver = st.gameOver,
            winner = st.winner,
            mode = st.mode,
            isMyTurn = st.isMyTurn,
            winningCells = st.winningCells.toList(),
            moves = st.moves,
            startedAt = st.startedAt,
            elapsedMs = st.elapsedMs,
            moveHistory = st.moveHistory,
            tag = st.tag
        )

    private fun applySaveGame(sg: SaveGame) {
        val msg = when {
            sg.gameOver && sg.winner == Cell.P1 -> "¡Gana Jugador 1 (Rojo)!"
            sg.gameOver && sg.winner == Cell.P2 -> "¡Gana Jugador 2 (Amarillo)!"
            sg.gameOver                        -> "Empate"
            sg.currentPlayer == Cell.P1        -> "Turno: Jugador 1 (Rojo)"
            else                               -> "Turno: Jugador 2 (Amarillo)"
        }
        val st = state.value
        state.value = st.copy(
            cols = sg.cols,
            rows = sg.rows,
            board = sg.board,
            currentPlayer = sg.currentPlayer,
            gameOver = sg.gameOver,
            winner = sg.winner,
            winningCells = sg.winningCells.toSet(),
            score = st.score.copy(
                p1Wins = sg.scoreP1,
                p2Wins = sg.scoreP2,
                draws = sg.draws
            ),
            mode = sg.mode,
            isMyTurn = sg.isMyTurn,
            moves = sg.moves,
            startedAt = sg.startedAt,
            elapsedMs = sg.elapsedMs,
            moveHistory = sg.moveHistory,
            tag = sg.tag,
            message = msg
        )
    }

    // ----------------- HELPERS -----------------

    private fun nextPlayer(p: Cell) = if (p == Cell.P1) Cell.P2 else Cell.P1

    private fun findAvailableRow(board: List<List<Cell>>, rows: Int, c: Int): Int? {
        for (r in rows - 1 downTo 0) if (board[r][c] == Cell.EMPTY) return r
        return null
    }

    private fun firstFreeRow(board: List<List<Cell>>, col: Int): Int? {
        for (r in board.size - 1 downTo 0) if (board[r][col] == Cell.EMPTY) return r
        return null
    }

    private fun deepCopyBoard(src: List<List<Cell>>): MutableList<MutableList<Cell>> =
        src.map { it.toMutableList() }.toMutableList()

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
            if (cells.size >= 4) return true to cells.take(4).toSet()
        }
        return false to emptySet()
    }

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

        var x = c
        var y = r
        while (inside(x, y) && board[y][x] == player) {
            x -= dx; y -= dy
        }
        x += dx; y += dy

        while (inside(x, y) && board[y][x] == player) {
            acc.add(y to x)
            x += dx; y += dy
        }

        return acc
    }
}
