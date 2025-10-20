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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    conecta4screen(vm)
                }
            }
        }
    }
}

@Composable
fun conecta4screen(vm: GameViewModel) {
    val st = vm.state
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conecta 4 - 1v1", fontWeight = FontWeight.SemiBold) }
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

            // Indicador de turno / estado
            TurnIndicator(message = st.message, current = st.currentPlayer, gameOver = st.gameOver)

            // Tablero
            Board(
                rows = st.rows,
                cols = st.cols,
                board = st.board,
                winning = st.winningCells,
                onColumnClick = { col -> vm.playColumn(col) },
                enabled = !st.gameOver
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

@Composable
fun ScoreRow(score: Score) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
        modifier = Modifier
            .fillMaxWidth(),
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
    // Contenedor del tablero
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(7f / 6f) // Mantener proporción del grid 7x6
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF1565C0)) // Azul "tablero"
                .padding(8.dp)
        ) {
            // Grid manual (6 filas x 7 columnas)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Dibujamos de arriba hacia abajo, pero recordemos que la "gravedad" cae al fondo.
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
    // Animación de aparición ligera
    val scale by animateFloatAsState(
        targetValue = if (cell != Cell.EMPTY) 1f else 0.98f,
        animationSpec = spring(),
        label = "scale"
    )

    val pieceColor = when (cell) {
        Cell.EMPTY -> Color.White
        Cell.P1 -> Color(0xFFD32F2F)   // Rojo
        Cell.P2 -> Color(0xFFFBC02D)   // Amarillo
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
