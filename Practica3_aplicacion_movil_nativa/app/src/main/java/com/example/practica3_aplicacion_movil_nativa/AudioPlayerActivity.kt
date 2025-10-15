package com.example.practica3_aplicacion_movil_nativa

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AudioPlayerActivity : BaseActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null

    private lateinit var btnPlayPause: Button
    private lateinit var btnStop: Button
    private lateinit var seekBar: SeekBar
    private lateinit var tvTitle: TextView
    private lateinit var tvTime: TextView
    private lateinit var waveformView: WaveformView

    private lateinit var btnShare: Button
    private lateinit var btnRename: Button
    private lateinit var btnDelete: Button

    private lateinit var audioUri: Uri
    private var audioName: String = "Grabación"

    private val handler = Handler(Looper.getMainLooper())
    private val updater = object : Runnable {
        override fun run() {
            mediaPlayer?.let { mp ->
                val pos = runCatching { mp.currentPosition }.getOrDefault(0)
                val dur = runCatching { mp.duration }.getOrDefault(0)
                seekBar.max = dur
                seekBar.progress = pos
                tvTime.text = "${formatMs(pos)} / ${formatMs(dur)}"
                handler.postDelayed(this, 500)
            }
        }
    }

    // Request codes
    private val REQ_READ = 401
    private val REQ_DELETE = 1001
    private val REQ_WRITE = 1002

    private var pendingNewName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnStop = findViewById(R.id.btnStop)
        seekBar = findViewById(R.id.seekBar)
        tvTitle = findViewById(R.id.tvTitle)
        tvTime = findViewById(R.id.tvTime)
        waveformView = findViewById(R.id.waveformView)

        btnShare = findViewById(R.id.btnShare)
        btnRename = findViewById(R.id.btnRename)
        btnDelete = findViewById(R.id.btnDelete)

        val uriStr = intent.getStringExtra("uri")
        audioName = intent.getStringExtra("name") ?: "Grabación"
        if (uriStr.isNullOrBlank()) { Toast.makeText(this, "Audio inválido", Toast.LENGTH_SHORT).show(); finish(); return }
        audioUri = Uri.parse(uriStr)
        tvTitle.text = audioName

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (!hasReadMediaPermission()) {
            requestReadMediaPermission()
        } else {
            if (!initPlayerFromContentUri(audioUri)) {
                Toast.makeText(this, "No se pudo abrir el audio", Toast.LENGTH_SHORT).show()
                finish(); return
            }
        }

        btnPlayPause.setOnClickListener {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.pause()
                        btnPlayPause.text = "Reproducir"
                        handler.removeCallbacks(updater)
                        setVisualizerEnabled(false)
                    } else {
                        mp.start()
                        btnPlayPause.text = "Pausar"
                        handler.post(updater)
                        setVisualizerEnabled(true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error reproduciendo audio", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnStop.setOnClickListener { stopAndReset() }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) runCatching { mediaPlayer?.seekTo(progress) }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Acciones
        btnShare.setOnClickListener { shareAudio() }
        btnRename.setOnClickListener { promptRename() }
        btnDelete.setOnClickListener { confirmDelete() }
    }

    // ---------- Permisos lectura ----------
    private fun hasReadMediaPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        else
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestReadMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), REQ_READ)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_READ)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_READ) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                if (!initPlayerFromContentUri(audioUri)) {
                    Toast.makeText(this, "No se pudo abrir el audio", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Permiso de lectura requerido", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // ---------- Inicializar MediaPlayer + Visualizer ----------
    private fun initPlayerFromContentUri(uri: Uri): Boolean {
        return runCatching {
            val afd = contentResolver.openAssetFileDescriptor(uri, "r") ?: return false
            val mp = MediaPlayer()
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.setOnCompletionListener {
                btnPlayPause.text = "Reproducir"
                handler.removeCallbacks(updater)
                seekBar.progress = 0
                setVisualizerEnabled(false)
            }
            mp.prepare()
            mediaPlayer = mp

            val dur = runCatching { mp.duration }.getOrDefault(0)
            tvTime.text = "00:00 / ${formatMs(dur)}"
            seekBar.max = dur

            setupVisualizer(mp.audioSessionId)
            true
        }.getOrDefault(false)
    }

    private fun setupVisualizer(audioSessionId: Int) {
        releaseVisualizer()
        // Algunos dispositivos requieren tamaño de captura específico
        val v = Visualizer(audioSessionId)
        visualizer = v
        // Rango de 0..255 bytes
        val captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(2048)
        v.captureSize = captureSize
        v.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(
                visualizer: Visualizer?,
                waveform: ByteArray?,
                samplingRate: Int
            ) {
                waveform?.let { waveformView.setWaveform(it) }
            }

            override fun onFftDataCapture(
                visualizer: Visualizer?,
                fft: ByteArray?,
                samplingRate: Int
            ) {
                // No usamos FFT en esta versión (solo waveform)
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false)
        // No lo habilitamos hasta que el usuario presione Play
        v.enabled = false
    }

    private fun setVisualizerEnabled(enable: Boolean) {
        try {
            visualizer?.enabled = enable
        } catch (_: Exception) { /* algunos OEM lanzan IllegalState si no está listo */ }
    }

    private fun releaseVisualizer() {
        runCatching { visualizer?.release() }
        visualizer = null
    }

    private fun stopAndReset() {
        runCatching { mediaPlayer?.pause(); mediaPlayer?.seekTo(0) }
        btnPlayPause.text = "Reproducir"
        handler.removeCallbacks(updater)
        seekBar.progress = 0
        setVisualizerEnabled(false)
    }

    private fun stopPlayback() {
        handler.removeCallbacks(updater)
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        releaseVisualizer()
    }

    private fun formatMs(ms: Int): String {
        val totalSec = ms / 1000
        val mm = totalSec / 60
        val ss = totalSec % 60
        return String.format("%02d:%02d", mm, ss)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }

    // ---------- Compartir ----------
    private fun shareAudio() {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, audioUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(i, "Compartir audio"))
    }

    // ---------- Renombrar ----------
    private fun promptRename() {
        val input = EditText(this).apply {
            val (base, _) = splitNameExt(audioName)
            setText(base)
            setSelection(text.length)
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Renombrar")
            .setMessage("Nuevo nombre (sin extensión):")
            .setView(input)
            .setPositiveButton("Guardar") { d, _ ->
                val newBase = input.text.toString().trim()
                if (newBase.isEmpty()) {
                    Toast.makeText(this, "Nombre vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                attemptRename(newBase)
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun attemptRename(newBase: String) {
        val (oldBase, ext) = splitNameExt(audioName)
        val newName = if (ext.isNotEmpty()) "$newBase.$ext" else newBase
        pendingNewName = newName

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
        }
        try {
            val rows = contentResolver.update(audioUri, values, null, null)
            if (rows > 0) {
                audioName = newName
                tvTitle.text = audioName
                Toast.makeText(this, "Renombrado", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
            } else {
                Toast.makeText(this, "No se pudo renombrar", Toast.LENGTH_SHORT).show()
            }
        } catch (se: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pi = MediaStore.createWriteRequest(contentResolver, listOf(audioUri))
                startIntentSenderForResult(pi.intentSender, REQ_WRITE, null, 0, 0, 0, null)
            } else {
                Toast.makeText(this, "Permiso insuficiente para renombrar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Eliminar ----------
    private fun confirmDelete() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar audio")
            .setMessage("¿Seguro que deseas eliminar este archivo?")
            .setPositiveButton("Eliminar") { d, _ ->
                requestDelete()
                d.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun requestDelete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = MediaStore.createDeleteRequest(contentResolver, listOf(audioUri))
            startIntentSenderForResult(pi.intentSender, REQ_DELETE, null, 0, 0, 0, null)
        } else {
            try {
                val rows = contentResolver.delete(audioUri, null, null)
                if (rows > 0) {
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(this, "Permiso insuficiente para eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Resultados ----------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_DELETE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQ_WRITE) {
            if (resultCode == Activity.RESULT_OK) {
                pendingNewName?.let { name ->
                    pendingNewName = null
                    attemptRename(name.removeSuffix(".")) // reintenta
                }
            } else {
                pendingNewName = null
                Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Utilidades ----------
    private fun splitNameExt(displayName: String): Pair<String, String> {
        val dot = displayName.lastIndexOf('.')
        return if (dot > 0 && dot < displayName.length - 1) {
            displayName.substring(0, dot) to displayName.substring(dot + 1)
        } else displayName to ""
    }
}
