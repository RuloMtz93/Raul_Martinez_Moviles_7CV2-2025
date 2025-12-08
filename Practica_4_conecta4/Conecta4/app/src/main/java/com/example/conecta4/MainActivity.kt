@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.conecta4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conecta4.bluetooth.BluetoothScreen
import com.example.conecta4.data.prefs.PrefsRepo
import com.example.conecta4.data.save.SaveFormat
import com.example.conecta4.ui.theme.ConectaTheme
import com.example.conecta4.sensors.rememberProximityState
import com.example.conecta4.sensors.rememberAmbientLuxState


// 👇 añadimos SAVES
private enum class Screen { MENU, GAME, BLUETOOTH, STATS, SETTINGS, SAVES }

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inits necesarios
        com.example.conecta4.data.stats.StatsRepo.init(applicationContext)
        PrefsRepo.init(applicationContext)

        setContent {
            val prefs by PrefsRepo.observe().collectAsState(initial = com.example.conecta4.data.prefs.AppPrefs())

            ConectaTheme(brand = prefs.themeBrand, darkMode = prefs.darkMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    var currentScreen by remember { mutableStateOf(Screen.MENU) }

                    when (currentScreen) {
                        Screen.MENU -> MainMenuScreen(
                            onPlayLocal = {
                                vm.setMode(GameMode.LOCAL)
                                currentScreen = Screen.GAME
                            },
                            onPlayAI = {
                                vm.setMode(GameMode.AI)
                                currentScreen = Screen.GAME
                            },
                            onPlayBT = { currentScreen = Screen.BLUETOOTH },
                            onStats = { currentScreen = Screen.STATS },
                            onSettings = { currentScreen = Screen.SETTINGS },
                            onSaves = { currentScreen = Screen.SAVES } // 👈 nuevo
                        )

                        Screen.BLUETOOTH -> BluetoothScreen(
                            vm = vm,
                            onConnectedNavigateToGame = { currentScreen = Screen.GAME },
                            onBack = { currentScreen = Screen.MENU }
                        )

                        Screen.GAME -> Conecta4Screen(
                            vm = vm,
                            prefsDefaultFormat = prefs.defaultFormat,
                            onBack = { currentScreen = Screen.MENU }
                        )

                        Screen.STATS -> StatsScreen(onBack = { currentScreen = Screen.MENU })

                        Screen.SETTINGS -> SettingsScreen(
                            prefs = prefs,
                            onBack = { currentScreen = Screen.MENU }
                        )

                        // 👇 nueva pantalla de gestor de partidas
                        Screen.SAVES -> SavesScreen(
                            onBack = { currentScreen = Screen.MENU },
                            onLoadWithFormat = { fmt ->
                                val ok = vm.loadGame(fmt)
                                if (ok) currentScreen = Screen.GAME
                                ok
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    onPlayLocal: () -> Unit,
    onPlayAI: () -> Unit,
    onPlayBT: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    onSaves: () -> Unit // 👈 nuevo
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Conecta 4", fontWeight = FontWeight.SemiBold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onPlayLocal) { Text("Jugar Local") }
            Button(onClick = onPlayAI)    { Text("Jugar vs IA") }
            OutlinedButton(onClick = onPlayBT) { Text("Jugar por Bluetooth") }
            OutlinedButton(onClick = onStats)  { Text("Historial (Room)") }
            OutlinedButton(onClick = onSettings) { Text("Opciones") }
            OutlinedButton(onClick = onSaves) { Text("Partidas guardadas") } // 👈 botón nuevo
        }
    }
}

@Composable
fun Conecta4Screen(
    vm: GameViewModel,
    prefsDefaultFormat: SaveFormat,
    onBack: () -> Unit
) {
    val st = vm.state.value
    // Sensores
    val isNear by rememberProximityState()     // true = cerca (bloquear tablero)
    val ambientLux by rememberAmbientLuxState() // lux de luz ambiental (null si no hay sensor)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = when (st.mode) {
                        GameMode.LOCAL -> "Conecta 4 - Local"
                        GameMode.AI -> "Conecta 4 - vs IA"
                        GameMode.BT -> "Conecta 4 - Bluetooth"
                    }
                    Text(title, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("Menú") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Marcador
            ScoreRow(score = st.score)

            // Guardar/Cargar con default + menú desplegable
            SaveLoadRow(vm = vm, defaultFormat = prefsDefaultFormat)

            // Indicador de turno / estado
            TurnIndicator(message = st.message, current = st.currentPlayer, gameOver = st.gameOver)

            // Indicadores de sensores
            if (isNear || ambientLux != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        if (ambientLux != null) {
                            val luxText = String.format("%.0f lx", ambientLux!!)
                            Text("Luz ambiental: $luxText",
                                style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("Luz ambiental: (no disponible)",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        if (isNear) {
                            Text("Proximidad: CERCA (tablero bloqueado)",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("Proximidad: lejos",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Tablero
            Board(
                rows = st.rows,
                cols = st.cols,
                board = st.board,
                winning = st.winningCells,
                onColumnClick = { col -> vm.playColumn(col) },
                enabled = !st.gameOver && (st.mode != GameMode.BT || st.isMyTurn) &&
                        !isNear // 👈 bloquea si hay algo cerca
            )

            // Botones de control
            ControlButtons(
                onResetRound = { vm.resetRound() },
                onResetAll = { vm.resetAll() },
                gameOver = st.gameOver
            )
        }
    }
}

/* =======================
   Guardar/Cargar con menú
   ======================= */

@Composable
fun SaveLoadRow(vm: GameViewModel, defaultFormat: SaveFormat) {
    var msg by remember { mutableStateOf<String?>(null) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Guardar: tap = default, chevron = menú
        ActionMenuButtonDefault(
            label = "Guardar (${defaultFormat.name})",
            options = listOf(
                "JSON" to SaveFormat.JSON,
                "XML"  to SaveFormat.XML,
                "TXT"  to SaveFormat.TEXT
            ),
            onPrimaryClick = {
                val path = vm.saveGame(defaultFormat)
                msg = "Guardado ${defaultFormat.name} en:\n$path"
            },
            onSelected = { fmt ->
                val path = vm.saveGame(fmt)
                msg = "Guardado ${fmt.name} en:\n$path"
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
        )

        // Cargar: tap = default, chevron = menú
        ActionMenuButtonDefault(
            label = "Cargar (${defaultFormat.name})",
            options = listOf(
                "JSON" to SaveFormat.JSON,
                "XML"  to SaveFormat.XML,
                "TXT"  to SaveFormat.TEXT
            ),
            onPrimaryClick = {
                val ok = vm.loadGame(defaultFormat)
                msg = if (ok) "Partida cargada (${defaultFormat.name})" else "No hay ${defaultFormat.name} previo"
            },
            onSelected = { fmt ->
                val ok = vm.loadGame(fmt)
                msg = if (ok) "Partida cargada (${fmt.name})" else "No hay ${fmt.name} previo"
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
        )
    }
    msg?.let {
        Text(
            text = it,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun ActionMenuButtonDefault(
    label: String,
    options: List<Pair<String, SaveFormat>>,
    onPrimaryClick: () -> Unit,
    onSelected: (SaveFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
        ) { Text(label) }

        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (text, fmt) ->
                DropdownMenuItem(text = { Text(text) }, onClick = {
                    expanded = false
                    onSelected(fmt)
                })
            }
        }
    }
}

/* ============================
   UI que ya tenías (sin cambios)
   ============================ */

@Composable
fun ScoreRow(score: Score) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScorePill(label = "J1 (Rojo)", value = score.p1Wins, color = Color(0xFFD32F2F))
            ScorePill(label = "Empates", value = score.draws, color = Color(0xFF455A64))
            ScorePill(label = "J2 (Amarillo)", value = score.p2Wins, color = Color(0xFFFBC02D))
        }
    }
}

@Composable
fun ScorePill(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("$value", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun TurnIndicator(message: String, current: Cell, gameOver: Boolean) {
    val color = when {
        gameOver -> Color(0xFF26A69A)
        current == Cell.P1 -> Color(0xFFD32F2F)
        else -> Color(0xFFFBC02D)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(message, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun Board(
    rows: Int,
    cols: Int,
    board: List<List<Cell>>,
    winning: Set<Pair<Int, Int>>,
    onColumnClick: (Int) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(7f / 6f)
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1565C0))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0 until cols) {
                            val cell = board[r][c]
                            val isWinning = winning.contains(r to c)
                            CellView(
                                cell = cell,
                                highlight = isWinning,
                                size = 42.dp,
                                onClick = { if (enabled) onColumnClick(c) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CellView(
    cell: Cell,
    highlight: Boolean,
    size: Dp,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (cell != Cell.EMPTY) 1f else 0.98f,
        animationSpec = spring(),
        label = "scale"
    )

    val pieceColor = when (cell) {
        Cell.EMPTY -> Color.White
        Cell.P1 -> Color(0xFFD32F2F)
        Cell.P2 -> Color(0xFFFBC02D)
    }

    val borderCol = if (highlight) Color(0xFF00E676) else Color(0xFF0D47A1)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, borderCol, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = (cell != Cell.EMPTY),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.8f)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(pieceColor)
            )
        }
    }
}

@Composable
fun ControlButtons(
    onResetRound: () -> Unit,
    onResetAll: () -> Unit,
    gameOver: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = onResetRound,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(if (gameOver) "Nueva Partida" else "Reiniciar Ronda")
        }
        OutlinedButton(
            onClick = onResetAll,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Reiniciar Todo (Marcador)")
        }
    }
}
