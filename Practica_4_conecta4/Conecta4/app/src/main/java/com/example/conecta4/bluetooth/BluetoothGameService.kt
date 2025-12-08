package com.example.conecta4.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Servicio simple para juego P2P por Bluetooth (RFCOMM).
 * - Host: listen + accept
 * - Client: connect
 * - Ambos: comparten un único socket conectado, leen enteros (columna) y emiten por incomingMoves.
 */
object BluetoothGameService {

    private const val SERVICE_NAME = "Conecta4BT"
    // UUID FIJO: debe ser EXACTAMENTE el mismo en host y client
    private val APP_UUID: UUID = UUID.fromString("9a7f8b0c-4d0a-4f2c-b9ba-1b1a9f7f2e55")

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var serverJob: Job? = null
    private var readerJob: Job? = null

    // Estado de conexión
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected

    // Flujo de jugadas entrantes (columna)
    private val _incomingMoves = MutableSharedFlow<Int>(extraBufferCapacity = 16)
    val incomingMoves: SharedFlow<Int> get() = _incomingMoves

    // Socket conectado (compartido host/cliente)
    @Volatile
    private var socket: BluetoothSocket? = null

    // Para evitar lecturas duplicadas
    private val reading = AtomicBoolean(false)

    fun getBondedDevices(): Set<BluetoothDevice> = adapter?.bondedDevices ?: emptySet()

    fun hasAdapter(): Boolean = adapter != null

    fun isEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun startServer() {
        if (adapter == null) return
        stop() // limpia si hay algo previo

        serverJob = scope.launch {
            var serverSocket: BluetoothServerSocket? = null
            try {
                // create server socket
                serverSocket = if (Build.VERSION.SDK_INT >= 29) {
                    adapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
                } else {
                    adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
                }

                // esperar conexión (bloqueante)
                val accepted = serverSocket.accept()
                socket = accepted
                _isConnected.value = true

                // arrancar lectura
                startReader()

            } catch (e: IOException) {
                _isConnected.value = false
            } finally {
                try { serverSocket?.close() } catch (_: IOException) {}
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startClient(device: BluetoothDevice) {
        if (adapter == null) return
        stop() // limpia si hay algo previo

        scope.launch {
            try {
                val tmpSocket = device.createRfcommSocketToServiceRecord(APP_UUID)
                // Recomendación clásica: cancelar discovery antes de conectar (evita lentitud)
                runCatching { adapter.cancelDiscovery() }
                tmpSocket.connect()
                socket = tmpSocket
                _isConnected.value = true

                // arrancar lectura
                startReader()

            } catch (e: IOException) {
                _isConnected.value = false
                try { socket?.close() } catch (_: IOException) {}
                socket = null
            }
        }
    }

    private fun startReader() {
        if (!reading.compareAndSet(false, true)) return // ya leyendo

        readerJob = scope.launch {
            val s = socket ?: return@launch
            try {
                val input = DataInputStream(s.inputStream)
                while (true) {
                    val col = input.readInt() // bloqueante
                    _incomingMoves.emit(col)
                }
            } catch (_: IOException) {
                // conexión cerrada
            } finally {
                reading.set(false)
                _isConnected.value = false
                try { s.close() } catch (_: IOException) {}
                socket = null
            }
        }
    }

    fun sendMove(col: Int) {
        scope.launch {
            try {
                val s = socket ?: return@launch
                val out = DataOutputStream(s.outputStream)
                out.writeInt(col)
                out.flush()
            } catch (_: IOException) {
                // si falla, cerramos
                closeInternal()
            }
        }
    }

    fun stop() {
        scope.launch {
            serverJob?.cancel()
            readerJob?.cancel()
            serverJob?.join()
            readerJob?.join()
            closeInternal()
        }
    }

    private fun closeInternal() {
        _isConnected.value = false
        try { socket?.close() } catch (_: IOException) {}
        socket = null
        reading.set(false)
    }
}
