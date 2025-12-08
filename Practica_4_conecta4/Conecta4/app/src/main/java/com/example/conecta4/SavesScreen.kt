@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.conecta4

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.conecta4.data.save.SaveFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavesScreen(
    onBack: () -> Unit,
    onLoadWithFormat: (SaveFormat) -> Boolean
) {
    val ctx = LocalContext.current
    val savesDir = remember { File(ctx.filesDir, "saves") }
    var files by remember { mutableStateOf(listSaveFiles(savesDir)) }

    var viewer by remember { mutableStateOf<File?>(null) }
    var viewerText by remember { mutableStateOf<String?>(null) }
    var snack by remember { mutableStateOf<String?>(null) }

    // Export launcher (SAF)
    var fileToExport by remember { mutableStateOf<File?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val f = fileToExport
        fileToExport = null
        if (uri != null && f != null) {
            val ok = exportFile(ctx, f, uri)
            snack = if (ok) "Exportado: ${f.name}" else "No se pudo exportar"
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partidas guardadas") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { data ->
                Snackbar { Text(data.visuals.message) }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (files.isEmpty()) {
                Text("No hay archivos guardados aún.", style = MaterialTheme.typography.bodyMedium)
            } else {
                files.forEach { f ->
                    val meta = fileMeta(f)
                    ElevatedCard {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(f.name, style = MaterialTheme.typography.titleMedium)
                            Text(meta, style = MaterialTheme.typography.bodySmall)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Ver
                                OutlinedButton(onClick = {
                                    viewer = f
                                    viewerText = f.readText()
                                }) { Text("Ver") }

                                // Cargar (según extensión)
                                Button(onClick = {
                                    val fmt = formatFromExtension(f)
                                    val ok = fmt?.let { onLoadWithFormat(it) } ?: false
                                    snack = if (ok) "Partida cargada (${fmt?.name ?: "?"})"
                                    else "No se pudo cargar (${fmt?.name ?: "?"})"
                                }) { Text("Cargar") }

                                // Exportar (SAF)
                                OutlinedButton(onClick = {
                                    fileToExport = f
                                    exportLauncher.launch(f.name)
                                }) { Text("Exportar") }

                                // Borrar
                                TextButton(onClick = {
                                    val ok = f.delete()
                                    snack = if (ok) "Eliminado: ${f.name}" else "No se pudo eliminar"
                                    files = listSaveFiles(savesDir)
                                }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Viewer de archivo en un diálogo
    if (viewer != null && viewerText != null) {
        AlertDialog(
            onDismissRequest = { viewer = null; viewerText = null },
            confirmButton = {
                TextButton(onClick = { viewer = null; viewerText = null }) { Text("Cerrar") }
            },
            title = { Text("Contenido: ${viewer!!.name}") },
            text = {
                SelectionContainer {
                    Column(Modifier.fillMaxWidth()) {
                        // Scroll horizontal para JSON/XML largos
                        Text(
                            viewerText!!,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .heightIn(min = 120.dp)
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        )
    }

    // Snackbar manual (simple)
    snack?.let {
        LaunchedEffect(it) {
            // usa ScaffoldState si quieres snack con animación;
            // aquí solo mostramos un diálogo temporal simple
        }
        // Mini aviso
        AlertDialog(
            onDismissRequest = { snack = null },
            confirmButton = { TextButton(onClick = { snack = null }) { Text("OK") } },
            title = { Text("Aviso") },
            text = { Text(it) }
        )
    }
}

private fun listSaveFiles(dir: File): List<File> {
    if (!dir.exists()) return emptyList()
    // Filtra solo extensiones soportadas
    return dir.listFiles { f ->
        f.isFile && (f.name.endsWith(".json", true) ||
                f.name.endsWith(".xml", true) ||
                f.name.endsWith(".txt", true))
    }?.sortedByDescending { it.lastModified() } ?: emptyList()
}

private fun fileMeta(f: File): String {
    val fmt = formatFromExtension(f)?.name ?: "?"
    val date = Date(f.lastModified())
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val kb = (f.length().coerceAtLeast(1L) + 1023) / 1024
    return "Formato: $fmt • Tamaño: ${kb}KB • Modificado: ${sdf.format(date)}"
}

private fun formatFromExtension(f: File): SaveFormat? =
    when {
        f.name.endsWith(".json", true) -> SaveFormat.JSON
        f.name.endsWith(".xml", true)  -> SaveFormat.XML
        f.name.endsWith(".txt", true)  -> SaveFormat.TEXT
        else -> null
    }

private fun exportFile(ctx: Context, src: File, destUri: Uri): Boolean = try {
    ctx.contentResolver.openOutputStream(destUri)?.use { out ->
        src.inputStream().use { ins -> ins.copyTo(out) }
    }
    true
} catch (_: Throwable) { false }
