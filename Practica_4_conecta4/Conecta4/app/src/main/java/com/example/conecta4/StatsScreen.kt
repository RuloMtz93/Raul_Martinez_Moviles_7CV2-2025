@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.conecta4

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.conecta4.data.stats.GameStat
import com.example.conecta4.data.stats.StatsRepo
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(onBack: () -> Unit) {
    val stats = remember { mutableStateListOf<GameStat>() }

    LaunchedEffect(Unit) {
        StatsRepo.observeAll().collectLatest { list ->
            stats.clear()
            stats.addAll(list)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Partidas") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        }
    ) { pad ->
        StatsListContent(pad, stats)
    }
}

@Composable
private fun StatsListContent(pad: PaddingValues, stats: List<GameStat>) {
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    LazyColumn(
        modifier = Modifier
            .padding(pad)
            .padding(12.dp)
    ) {
        items(stats, key = { it.id }) { s ->
            ElevatedCard(modifier = Modifier.padding(bottom = 8.dp)) {
                androidx.compose.foundation.layout.Column(Modifier.padding(12.dp)) {
                    Text("Fecha: ${fmt.format(Date(s.timestamp))}", style = MaterialTheme.typography.bodyMedium)
                    Text("Modo: ${s.mode}", style = MaterialTheme.typography.bodyMedium)
                    Text("Resultado: ${s.result}", style = MaterialTheme.typography.bodyMedium)
                    Text("Jugadas: ${s.moves}", style = MaterialTheme.typography.bodyMedium)
                    Text("Duración: ${s.durationMs / 1000}s", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
