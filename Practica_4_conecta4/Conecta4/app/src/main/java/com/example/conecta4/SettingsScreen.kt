@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.conecta4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.conecta4.data.prefs.DarkMode
import com.example.conecta4.data.prefs.PrefsRepo
import com.example.conecta4.data.prefs.ThemeBrand
import com.example.conecta4.data.save.SaveFormat
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: com.example.conecta4.data.prefs.AppPrefs,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opciones") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atrás") } }
            )
        }
    ) { pad ->
        Content(
            pad = pad,
            prefs = prefs,
            onSetFormat = { scope.launch { PrefsRepo.setDefaultFormat(it) } },
            onSetBrand = { scope.launch { PrefsRepo.setThemeBrand(it) } },
            onSetDark  = { scope.launch { PrefsRepo.setDarkMode(it) } },
            onSetSound = { scope.launch { PrefsRepo.setSoundEnabled(it) } }
        )
    }
}

@Composable
private fun Content(
    pad: PaddingValues,
    prefs: com.example.conecta4.data.prefs.AppPrefs,
    onSetFormat: (SaveFormat) -> Unit,
    onSetBrand: (ThemeBrand) -> Unit,
    onSetDark: (DarkMode) -> Unit,
    onSetSound: (Boolean) -> Unit
) {
    Column(
        Modifier.padding(pad).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Formato por defecto")
                DropdownPref(
                    current = prefs.defaultFormat.name,
                    items = SaveFormat.values().map { it.name },
                    onPick = { name -> onSetFormat(SaveFormat.valueOf(name)) }
                )
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tema de color")
                DropdownPref(
                    current = prefs.themeBrand.name,
                    items = ThemeBrand.values().map { it.name },
                    onPick = { name -> onSetBrand(ThemeBrand.valueOf(name)) }
                )
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Modo de apariencia")
                DropdownPref(
                    current = prefs.darkMode.name,
                    items = DarkMode.values().map { it.name },
                    onPick = { name -> onSetDark(DarkMode.valueOf(name)) }
                )
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sonidos habilitados")
                Switch(checked = prefs.soundEnabled, onCheckedChange = onSetSound)
            }
        }
    }
}

@Composable
private fun DropdownPref(
    current: String,
    items: List<String>,
    onPick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(current)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        items.forEach { item ->
            DropdownMenuItem(text = { Text(item) }, onClick = {
                expanded = false
                onPick(item)
            })
        }
    }
}
