package com.example.practica3_aplicacion_movil_nativa

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer
import com.example.practica3_aplicacion_movil_nativa.hapticTick
import com.example.practica3_aplicacion_movil_nativa.vibrateShort


class AudioRecorderActivity : BaseActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnResume: Button
    private lateinit var btnStop: Button
    private lateinit var chronometer: Chronometer
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerDuration: Spinner
    private lateinit var spinnerQuality: Spinner
    private lateinit var spinnerFormat: Spinner

    private var mediaRecorder: MediaRecorder? = null
    private var timerTask: Timer? = null
    private var maxDurationMs: Int = 60_000 // 60s default

    // Para guardar en MediaStore
    private var currentAudioUri: Uri? = null
    private var currentPfd: ParcelFileDescriptor? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_recorder)

        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnResume = findViewById(R.id.btnResume)
        btnStop = findViewById(R.id.btnStop)
        chronometer = findViewById(R.id.chronometer)
        progressBar = findViewById(R.id.progressBar)
        spinnerDuration = findViewById(R.id.spinnerDuration)
        spinnerQuality = findViewById(R.id.spinnerQuality)
        spinnerFormat = findViewById(R.id.spinnerFormat)

        // Duraciones
        val durations = arrayOf("10 segundos", "30 segundos", "60 segundos", "120 segundos")
        spinnerDuration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durations)

        // Calidad
        val qualities = arrayOf("Baja", "Media", "Alta")
        spinnerQuality.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, qualities)

        // Formato (MediaRecorder soportado)
        val formats = arrayOf("M4A (AAC/MP4)", "AAC (.aac)")
        spinnerFormat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formats)

        // Permisos mínimos
        ensurePermissions()

        btnPause.isEnabled = false
        btnResume.isEnabled = false
        btnStop.isEnabled = false

        btnStart.setOnClickListener {
            it.hapticTick()
            startRecordingToMediaStore() // o startRecordingToMediaStore() si usaste la versión migrada
        }
        btnPause.setOnClickListener {
            it.hapticTick()
            pauseRecording()
        }
        btnResume.setOnClickListener {
            it.hapticTick()
            resumeRecording()
        }
        btnStop.setOnClickListener {
            it.hapticTick()
            stopRecordingAndFinalize()
            applicationContext.vibrateShort()
        }
    }

    private fun ensurePermissions() {
        val needs = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needs.add(Manifest.permission.RECORD_AUDIO)
        }
        // Para leer de galería en Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                needs.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            // Compatibilidad
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needs.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (needs.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needs.toTypedArray(), 101)
        }
    }

    // ----- Iniciar grabación a MediaStore con PFD -----
    private fun startRecordingToMediaStore() {
        if (isRecording) return

        // Duración seleccionada
        maxDurationMs = when (spinnerDuration.selectedItemPosition) {
            0 -> 10_000
            1 -> 30_000
            2 -> 60_000
            else -> 120_000
        }

        // Calidad -> bitrate / samplerate
        val (bitrate, sampleRate) = when (spinnerQuality.selectedItem.toString()) {
            "Baja" -> 64_000 to 16_000
            "Media" -> 128_000 to 44_100
            else -> 192_000 to 48_000
        }

        val isM4A = spinnerFormat.selectedItemPosition == 0
        val mime = if (isM4A) "audio/mp4" else "audio/aac"
        val ext = if (isM4A) "m4a" else "aac"
        val displayName = "REC_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.$ext"

        // 1) Crear entrada en MediaStore
        val cv = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, mime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Audio.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            put(MediaStore.Audio.Media.TITLE, displayName.removeSuffix(".$ext"))
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cv)
        if (uri == null) {
            Toast.makeText(this, "No se pudo crear el archivo en MediaStore", Toast.LENGTH_SHORT).show()
            return
        }
        currentAudioUri = uri

        try {
            // 2) Abrir FD para que MediaRecorder escriba directo
            currentPfd = resolver.openFileDescriptor(uri, "w")
            if (currentPfd == null) throw IOException("No se pudo abrir descriptor de archivo")

            // 3) Configurar MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                if (isM4A) {
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                } else {
                    // AAC ADTS (.aac)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    } else {
                        // Fallback: 3GP si no soporta AAC_ADTS (poco común desde minSdk 26)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    }
                }
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(bitrate)
                setAudioSamplingRate(sampleRate)
                setOutputFile(currentPfd!!.fileDescriptor)

                try {
                    prepare()
                    start()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@AudioRecorderActivity, "Error al iniciar grabación", Toast.LENGTH_SHORT).show()
                    cleanupFailedInsert()
                    return
                }
            }

            isRecording = true
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
            btnStart.isEnabled = false
            btnPause.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            btnResume.isEnabled = false
            btnStop.isEnabled = true

            startAudioLevelMonitoring()
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show()

            // Detener automáticamente al llegar al tiempo máximo
            Handler(Looper.getMainLooper()).postDelayed({
                if (isRecording) stopRecordingAndFinalize()
            }, maxDurationMs.toLong())

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al iniciar grabación", Toast.LENGTH_SHORT).show()
            cleanupFailedInsert()
        }
    }

    private fun startAudioLevelMonitoring() {
        timerTask?.cancel()
        timerTask = timer(period = 100) {
            val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
            val level = (maxAmplitude / 32767.0 * 100).toInt().coerceIn(0, 100)
            runOnUiThread { progressBar.progress = level }
        }
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                chronometer.stop()
                btnPause.isEnabled = false
                btnResume.isEnabled = true
                Toast.makeText(this, "Grabación en pausa", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "No se pudo pausar", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Pausar no soportado en esta versión de Android", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                chronometer.start()
                btnPause.isEnabled = true
                btnResume.isEnabled = false
                Toast.makeText(this, "Grabación reanudada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "No se pudo reanudar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecordingAndFinalize() {
        if (!isRecording) return
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show()
        } finally {
            mediaRecorder = null
            currentPfd?.close()
            currentPfd = null
            timerTask?.cancel()
            chronometer.stop()
            isRecording = false

            // Marcar como no pendiente en Q+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                currentAudioUri?.let { uri ->
                    val cv = ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) }
                    contentResolver.update(uri, cv, null, null)
                }
            }

            btnStart.isEnabled = true
            btnPause.isEnabled = false
            btnResume.isEnabled = false
            btnStop.isEnabled = false

            Toast.makeText(this, "Grabación guardada", Toast.LENGTH_LONG).show()
        }
    }

    private fun cleanupFailedInsert() {
        try { currentPfd?.close() } catch (_: Exception) {}
        currentPfd = null
        currentAudioUri?.let {
            try { contentResolver.delete(it, null, null) } catch (_: Exception) {}
        }
        currentAudioUri = null
        isRecording = false
    }

    override fun onDestroy() {
        super.onDestroy()
        timerTask?.cancel()
        try { mediaRecorder?.release() } catch (_: Exception) {}
        mediaRecorder = null
        try { currentPfd?.close() } catch (_: Exception) {}
        currentPfd = null
    }
}
