package com.example.conecta4.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.conecta4.GameMode
import com.example.conecta4.GameViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    vm: GameViewModel,
    onConnectedNavigateToGame: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as Activity

    // 1) Solicitud de permisos
    val neededPermissions = remember {
        if (Build.VERSION.SDK_INT >= 31) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* noop */ }

    LaunchedEffect(Unit) {
        val missing = neededPermissions.any {
            ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing) permissionLauncher.launch(neededPermissions)
    }

    // 2) Estado de conexión -> navegar cuando conecte
    val isConnected by BluetoothGameService.isConnected.collectAsState()
    LaunchedEffect(isConnected) {
        if (isConnected) {
            onConnectedNavigateToGame()
        }
    }

    // 3) UI
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bluetooth") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Estado: " + if (isConnected) "Conectado" else "Desconectado")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        BluetoothGameService.startServer()
                        vm.setMode(GameMode.BT, myTurnFirst = true)
                    }
                ) { Text("Anfitrión (Host)") }

                OutlinedButton(
                    onClick = {
                        // la lista de emparejados está abajo
                    }
                ) { Text("Invitado (Join)") }
            }

            Text("Dispositivos emparejados", fontWeight = FontWeight.Bold)

            val bondedDevices = remember { mutableStateListOf<BluetoothDevice>() }
            LaunchedEffect(Unit) {
                bondedDevices.clear()
                bondedDevices.addAll(BluetoothGameService.getBondedDevices())
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bondedDevices, key = { it.address }) { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                BluetoothGameService.startClient(device)
                                vm.setMode(GameMode.BT, myTurnFirst = false)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(device.name ?: "(sin nombre)", fontWeight = FontWeight.SemiBold)
                            Text(device.address)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isConnected) {
                Text(
                    "Instrucciones:\n" +
                            "1) Asegúrate de que ambos teléfonos están emparejados en Ajustes.\n" +
                            "2) Uno toca 'Anfitrión'. El otro, en 'Dispositivos emparejados', elige al anfitrión.\n" +
                            "3) Al conectar, volverás al tablero."
                )
            }

            if (isConnected) {
                Button(onClick = { BluetoothGameService.stop() }) { Text("Desconectar") }
            }
        }
    }
}
