package com.example.conecta4.data.save

import android.content.Context
import com.example.conecta4.Cell
import com.example.conecta4.GameMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Gestor de guardados en almacenamiento interno:
 * - JSON  (last_game.json)
 * - XML   (last_game.xml)
 * - TEXT  (last_game.txt)  <-- NUEVO
 *
 * Directorio: context.filesDir/saves
 */
object SaveManager {

    private const val DIR_NAME = "saves"
    private const val JSON_NAME = "last_game.json"
    private const val XML_NAME  = "last_game.xml"
    private const val TXT_NAME  = "last_game.txt"

    fun save(ctx: Context, data: SaveGame, format: SaveFormat): File {
        ensureDir(ctx)
        return when (format) {
            SaveFormat.JSON -> saveJson(ctx, data)
            SaveFormat.XML  -> saveXml(ctx, data)
            SaveFormat.TEXT -> saveTxt(ctx, data) // NUEVO
        }
    }

    fun load(ctx: Context, format: SaveFormat): SaveGame? {
        ensureDir(ctx)
        return when (format) {
            SaveFormat.JSON -> loadJson(ctx)
            SaveFormat.XML  -> loadXml(ctx)
            SaveFormat.TEXT -> loadTxt(ctx) // NUEVO
        }
    }

    // -------- Helpers de directorio/archivo --------

    private fun ensureDir(ctx: Context) {
        val dir = File(ctx.filesDir, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
    }

    private fun fileFor(ctx: Context, name: String) = File(File(ctx.filesDir, DIR_NAME), name)

    // ===== JSON =====

    private fun saveJson(ctx: Context, data: SaveGame): File {
        val obj = JSONObject().apply {
            put("cols", data.cols)
            put("rows", data.rows)
            put("board", toJsonBoard(data.board))
            put("currentPlayer", data.currentPlayer.name)
            put("scoreP1", data.scoreP1)
            put("scoreP2", data.scoreP2)
            put("draws", data.draws)
            put("gameOver", data.gameOver)
            put("winner", data.winner?.name)
            put("mode", data.mode.name)
            put("isMyTurn", data.isMyTurn)
            put("winningCells", toJsonPairs(data.winningCells))
            put("moves", data.moves)
            put("startedAt", data.startedAt)
            put("elapsedMs", data.elapsedMs)
            put("moveHistory", JSONArray().apply { data.moveHistory.forEach { put(it) } })
            put("tag", data.tag)
        }
        val f = fileFor(ctx, JSON_NAME)
        f.writeText(obj.toString(2))

        runCatching {
            val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val copy = File(f.parentFile, "save_$stamp.json")
            if (!copy.exists()) f.copyTo(copy, overwrite = false)
        }

        return f
    }

    private fun loadJson(ctx: Context): SaveGame? {
        return try {
            val f = fileFor(ctx, JSON_NAME)
            if (!f.exists()) return null
            val obj = JSONObject(f.readText())

            val cols = obj.optInt("cols", 7)
            val rows = obj.optInt("rows", 6)

            // Board tolerante: acepta strings ("P1") o enteros (1)
            val board = obj.optJSONArray("board")?.let { fromJsonBoardMixed(it, rows, cols) }
                ?: List(rows) { List(cols) { Cell.EMPTY } }

            // currentPlayer/winner tolerantes
            val currentPlayer = anyToCell(obj.opt("currentPlayer")) ?: Cell.P1

            val scoreP1 = obj.optInt("scoreP1", 0)
            val scoreP2 = obj.optInt("scoreP2", 0)
            val draws   = obj.optInt("draws", 0)
            val gameOver = obj.optBoolean("gameOver", false)

            val winnerAny = obj.opt("winner") // puede ser null, "", "P1" o 1
            val winner = anyToCell(winnerAny)

            val modeStr = obj.optString("mode", "LOCAL")
            val mode = runCatching { GameMode.valueOf(modeStr) }.getOrNull() ?: GameMode.LOCAL

            val isMyTurn = obj.optBoolean("isMyTurn", true)
            val winningCells = obj.optJSONArray("winningCells")?.let { fromJsonPairs(it) } ?: emptyList()

            val moves = obj.optInt("moves", 0)
            val startedAt = obj.optLong("startedAt", System.currentTimeMillis())
            val elapsedMs = obj.optLong("elapsedMs", 0L)

            val moveHistory = obj.optJSONArray("moveHistory")?.let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    when (val v = arr.opt(i)) {
                        is Int -> v
                        is Number -> v.toInt()
                        is String -> v.toIntOrNull()
                        else -> null
                    }
                }
            } ?: emptyList()

            val tag = obj.optString("tag", null)?.takeIf { it.isNotBlank() }

            SaveGame(
                cols, rows, board, currentPlayer, scoreP1, scoreP2, draws,
                gameOver, winner, mode, isMyTurn, winningCells,
                moves, startedAt, elapsedMs, moveHistory, tag
            )
        } catch (_: Exception) {
            null
        }
    }

    // Acepta "EMPTY"/"P1"/"P2" o 0/1/2
    private fun anyToCell(v: Any?): Cell? = when (v) {
        is String -> runCatching { Cell.valueOf(v) }.getOrNull()
        is Int -> when (v) { 0 -> Cell.EMPTY; 1 -> Cell.P1; 2 -> Cell.P2; else -> null }
        is Number -> when (v.toInt()) { 0 -> Cell.EMPTY; 1 -> Cell.P1; 2 -> Cell.P2; else -> null }
        else -> null
    }

    /** Tablero tolerante: cada celda puede ser string o int */
    private fun fromJsonBoardMixed(arr: JSONArray, rows: Int, cols: Int): List<List<Cell>> {
        val out = MutableList(rows) { MutableList(cols) { Cell.EMPTY } }
        val rr = minOf(arr.length(), rows)
        for (r in 0 until rr) {
            val row = arr.optJSONArray(r) ?: continue
            val cc = minOf(row.length(), cols)
            for (c in 0 until cc) {
                out[r][c] = anyToCell(row.opt(c)) ?: Cell.EMPTY
            }
        }
        return out.map { it.toList() }
    }



    private fun toJsonBoard(board: List<List<Cell>>): JSONArray =
        JSONArray().apply {
            board.forEach { row ->
                put(JSONArray().apply {
                    row.forEach { put(it.name) }
                })
            }
        }

    private fun fromJsonBoard(arr: JSONArray): List<List<Cell>> =
        (0 until arr.length()).map { r ->
            val row = arr.getJSONArray(r)
            (0 until row.length()).map { c ->
                Cell.valueOf(row.getString(c))
            }
        }

    private fun toJsonPairs(list: List<Pair<Int, Int>>): JSONArray =
        JSONArray().apply { list.forEach { put(JSONArray().apply { put(it.first); put(it.second) }) } }

    private fun fromJsonPairs(arr: JSONArray): List<Pair<Int, Int>> =
        (0 until arr.length()).map { i ->
            val p = arr.getJSONArray(i)
            p.getInt(0) to p.getInt(1)
        }

    // ===== XML (string simple) =====

    private fun saveXml(ctx: Context, data: SaveGame): File {
        val sb = StringBuilder()
        sb.appendLine("<save>")
        sb.appendLine("  <cols>${data.cols}</cols>")
        sb.appendLine("  <rows>${data.rows}</rows>")
        sb.appendLine("  <board>")
        data.board.forEach { row ->
            sb.append("    <row>")
            sb.append(row.joinToString(",") { it.name })
            sb.appendLine("</row>")
        }
        sb.appendLine("  </board>")
        sb.appendLine("  <currentPlayer>${data.currentPlayer.name}</currentPlayer>")
        sb.appendLine("  <scoreP1>${data.scoreP1}</scoreP1>")
        sb.appendLine("  <scoreP2>${data.scoreP2}</scoreP2>")
        sb.appendLine("  <draws>${data.draws}</draws>")
        sb.appendLine("  <gameOver>${data.gameOver}</gameOver>")
        sb.appendLine("  <winner>${data.winner?.name ?: ""}</winner>")
        sb.appendLine("  <mode>${data.mode.name}</mode>")
        sb.appendLine("  <isMyTurn>${data.isMyTurn}</isMyTurn>")
        sb.appendLine("  <winningCells>")
        data.winningCells.forEach { (r, c) ->
            sb.appendLine("    <cell r=\"$r\" c=\"$c\"/>")
        }
        sb.appendLine("  </winningCells>")
        sb.appendLine("  <moves>${data.moves}</moves>")
        sb.appendLine("  <startedAt>${data.startedAt}</startedAt>")
        sb.appendLine("  <elapsedMs>${data.elapsedMs}</elapsedMs>")
        sb.appendLine("  <moveHistory>${data.moveHistory.joinToString(",")}</moveHistory>")
        sb.appendLine("  <tag>${data.tag.orEmpty()}</tag>")
        sb.appendLine("</save>")
        val f = fileFor(ctx, XML_NAME)
        f.writeText(sb.toString())
        return f
    }

    private fun loadXml(ctx: Context): SaveGame? {
        val f = fileFor(ctx, XML_NAME)
        if (!f.exists()) return null
        val text = f.readText()

        fun tag(name: String): String? {
            val open = "<$name>"
            val close = "</$name>"
            val i = text.indexOf(open)
            val j = text.indexOf(close)
            return if (i >= 0 && j > i) text.substring(i + open.length, j).trim() else null
        }

        val cols = tag("cols")?.toIntOrNull() ?: return null
        val rows = tag("rows")?.toIntOrNull() ?: return null

        val boardBlock = tag("board").orEmpty()
        val rowRegex = Regex("<row>(.*?)</row>")
        val board: List<List<Cell>> = rowRegex.findAll(boardBlock).map { m ->
            m.groupValues[1].split(",").map { Cell.valueOf(it) }
        }.toList()

        val currentPlayer = Cell.valueOf(tag("currentPlayer") ?: "P1")
        val scoreP1 = tag("scoreP1")?.toIntOrNull() ?: 0
        val scoreP2 = tag("scoreP2")?.toIntOrNull() ?: 0
        val draws = tag("draws")?.toIntOrNull() ?: 0
        val gameOver = tag("gameOver")?.toBooleanStrictOrNull() ?: false
        val w = tag("winner").orEmpty().trim()
        val winner = if (w.isEmpty()) null else Cell.valueOf(w)
        val mode = GameMode.valueOf(tag("mode") ?: "LOCAL")
        val isMyTurn = tag("isMyTurn")?.toBooleanStrictOrNull() ?: true

        val wcBlock = tag("winningCells").orEmpty()
        val cellRegex = Regex("<cell\\s+r=\"(\\d+)\"\\s+c=\"(\\d+)\"\\s*/>")
        val winningCells = cellRegex.findAll(wcBlock).map { it.groupValues[1].toInt() to it.groupValues[2].toInt() }.toList()

        val moves = tag("moves")?.toIntOrNull() ?: 0
        val startedAt = tag("startedAt")?.toLongOrNull() ?: System.currentTimeMillis()
        val elapsedMs = tag("elapsedMs")?.toLongOrNull() ?: 0L
        val moveHistory = tag("moveHistory").orEmpty().split(",").filter { it.isNotBlank() }.map { it.toInt() }
        val tagText = tag("tag").orEmpty().ifBlank { null }

        return SaveGame(
            cols, rows, board, currentPlayer, scoreP1, scoreP2, draws,
            gameOver, winner, mode, isMyTurn, winningCells,
            moves, startedAt, elapsedMs, moveHistory, tagText
        )
    }

    // ===== TEXT (key=value + CSV simples) =====

    private fun saveTxt(ctx: Context, data: SaveGame): File {
        val sb = StringBuilder()
        sb.appendLine("cols=${data.cols}")
        sb.appendLine("rows=${data.rows}")
        sb.appendLine("currentPlayer=${data.currentPlayer.name}")
        sb.appendLine("scoreP1=${data.scoreP1}")
        sb.appendLine("scoreP2=${data.scoreP2}")
        sb.appendLine("draws=${data.draws}")
        sb.appendLine("gameOver=${data.gameOver}")
        sb.appendLine("winner=${data.winner?.name ?: ""}")
        sb.appendLine("mode=${data.mode.name}")
        sb.appendLine("isMyTurn=${data.isMyTurn}")
        sb.appendLine("moves=${data.moves}")
        sb.appendLine("startedAt=${data.startedAt}")
        sb.appendLine("elapsedMs=${data.elapsedMs}")
        sb.appendLine("moveHistory=${data.moveHistory.joinToString(",")}")
        sb.appendLine("tag=${data.tag.orEmpty()}")

        // board como filas separadas por ';' y celdas por ','
        val boardStr = data.board.joinToString(";") { row -> row.joinToString(",") { it.name } }
        sb.appendLine("board=$boardStr")

        // winningCells como r:c;r:c;...
        val wc = data.winningCells.joinToString(";") { "${it.first}:${it.second}" }
        sb.appendLine("winningCells=$wc")

        val f = fileFor(ctx, TXT_NAME)
        f.writeText(sb.toString())
        return f
    }

    private fun loadTxt(ctx: Context): SaveGame? {
        val f = fileFor(ctx, TXT_NAME)
        if (!f.exists()) return null
        val map = mutableMapOf<String, String>()
        f.forEachLine { line ->
            val i = line.indexOf('=')
            if (i > 0) {
                val k = line.substring(0, i).trim()
                val v = line.substring(i + 1).trim()
                map[k] = v
            }
        }

        val cols = map["cols"]?.toIntOrNull() ?: return null
        val rows = map["rows"]?.toIntOrNull() ?: return null

        val boardStr = map["board"].orEmpty()
        val board: List<List<Cell>> =
            if (boardStr.isNotEmpty())
                boardStr.split(";").map { row -> row.split(",").map { Cell.valueOf(it) } }
            else
                List(rows) { List(cols) { Cell.EMPTY } }

        val currentPlayer = Cell.valueOf(map["currentPlayer"] ?: "P1")
        val scoreP1 = map["scoreP1"]?.toIntOrNull() ?: 0
        val scoreP2 = map["scoreP2"]?.toIntOrNull() ?: 0
        val draws = map["draws"]?.toIntOrNull() ?: 0
        val gameOver = map["gameOver"]?.toBoolean() ?: false
        val w = map["winner"].orEmpty()
        val winner = if (w.isEmpty()) null else Cell.valueOf(w)
        val mode = GameMode.valueOf(map["mode"] ?: "LOCAL")
        val isMyTurn = map["isMyTurn"]?.toBoolean() ?: true
        val moves = map["moves"]?.toIntOrNull() ?: 0
        val startedAt = map["startedAt"]?.toLongOrNull() ?: System.currentTimeMillis()
        val elapsedMs = map["elapsedMs"]?.toLongOrNull() ?: 0L

        val moveHistory = map["moveHistory"]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { it.toInt() }
            ?: emptyList()

        val wcStr = map["winningCells"].orEmpty()
        val winningCells = if (wcStr.isNotEmpty()) {
            wcStr.split(";").mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size == 2) parts[0].toIntOrNull()?.let { r ->
                    parts[1].toIntOrNull()?.let { c -> r to c }
                } else null
            }
        } else emptyList()

        val tagText = map["tag"].orEmpty().ifBlank { null }

        return SaveGame(
            cols, rows, board, currentPlayer, scoreP1, scoreP2, draws,
            gameOver, winner, mode, isMyTurn, winningCells,
            moves, startedAt, elapsedMs, moveHistory, tagText
        )
    }
}
