package com.example.practica3_aplicacion_movil_nativa

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.os.*
import android.provider.MediaStore
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.practica3_aplicacion_movil_nativa.Sounds
import com.example.practica3_aplicacion_movil_nativa.hapticTick
import com.example.practica3_aplicacion_movil_nativa.vibrateShort


// Si ya usas BaseActivity para aplicar tema y recrear en cambios, hereda de BaseActivity.
// Si no la tienes, puedes cambiar a : AppCompatActivity()
class CameraActivity : BaseActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: ImageView
    private lateinit var btnCapture: Button
    private lateinit var btnSwitchCamera: Button
    private lateinit var btnFlash: Button
    private lateinit var btnFilter: Button
    private lateinit var spinnerTimer: Spinner
    private lateinit var spinnerFormat: Spinner
    private lateinit var tvCountdown: TextView

    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashMode = ImageCapture.FLASH_MODE_AUTO
    private lateinit var cameraExecutor: ExecutorService
    private var selectedFilter = "Ninguno"
    private var selectedFormat = "JPEG" // "JPEG" | "PNG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        btnCapture = findViewById(R.id.btnCapture)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)
        btnFlash = findViewById(R.id.btnFlash)
        btnFilter = findViewById(R.id.btnFilter)
        spinnerTimer = findViewById(R.id.spinnerTimer)
        spinnerFormat = findViewById(R.id.spinnerFormat)
        tvCountdown = findViewById(R.id.tvCountdown)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            startCamera()
        }

        // Flash
        btnFlash.setOnClickListener {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> { btnFlash.text = "Flash ON"; ImageCapture.FLASH_MODE_ON }
                ImageCapture.FLASH_MODE_ON -> { btnFlash.text = "Flash OFF"; ImageCapture.FLASH_MODE_OFF }
                else -> { btnFlash.text = "Flash Auto"; ImageCapture.FLASH_MODE_AUTO }
            }
            imageCapture?.flashMode = flashMode
        }

        // Cambiar cámara
        btnSwitchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }

        // Temporizador
        val timerOptions = arrayOf("0", "3", "5", "10")
        spinnerTimer.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timerOptions)

        // Formato (JPEG/PNG)
        val formatOptions = arrayOf("JPEG", "PNG")
        spinnerFormat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formatOptions)
        spinnerFormat.setSelection(0) // JPEG por defecto
        spinnerFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedFormat = formatOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Filtro: ahora incluye "Contraste"
        btnFilter.setOnClickListener {
            val filters = arrayOf("Ninguno", "Grayscale", "Sepia", "Brillo", "Contraste")
            val nextIndex = (filters.indexOf(selectedFilter) + 1) % filters.size
            selectedFilter = filters[nextIndex]
            btnFilter.text = selectedFilter
        }

        // Captura
        btnCapture.setOnClickListener {
            // feedback háptico ligero + anim de “pulse”
            btnCapture.hapticTick()
            btnCapture.animate().scaleX(0.93f).scaleY(0.93f).setDuration(60).withEndAction {
                btnCapture.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
            }.start()
            val delay = spinnerTimer.selectedItem.toString().toInt()
            if (delay == 0) takePhoto() else startCountdown(delay)
        }
    }

    private fun startCountdown(seconds: Int) {
        tvCountdown.text = seconds.toString()
        tvCountdown.visibility = TextView.VISIBLE

        var remaining = seconds
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (remaining > 0) {
                    tvCountdown.text = remaining.toString()
                    remaining--
                    handler.postDelayed(this, 1000)
                } else {
                    tvCountdown.visibility = TextView.GONE
                    takePhoto()
                }
            }
        }
        handler.post(runnable)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .setTargetRotation(previewView.display.rotation)
                .build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analyzer ->
                    analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmapWithRotation(
                            isFrontCamera = (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                        ) ?: run { imageProxy.close(); return@setAnalyzer }
                        val filtered = applyFilter(bitmap, selectedFilter)
                        runOnUiThread { overlayView.setImageBitmap(filtered) }
                        imageProxy.close()
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)
            } catch (exc: Exception) {
                exc.printStackTrace()
                Toast.makeText(this, "Error iniciando cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.targetRotation = previewView.display.rotation

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmapWithRotation(
                        isFrontCamera = (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                    )
                    imageProxy.close()
                    if (bitmap == null) {
                        Toast.makeText(this@CameraActivity, "Error al procesar imagen", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val filteredBitmap = applyFilter(bitmap, selectedFilter)
                    val uri = saveBitmap(filteredBitmap, name, selectedFormat)
                    if (uri != null) {
                        // sonido de obturador + vibración corta
                        Sounds.shutter()
                        applicationContext.vibrateShort()
                        Toast.makeText(this@CameraActivity, "Foto guardada ($selectedFormat)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CameraActivity, "Error al guardar foto", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    Toast.makeText(this@CameraActivity, "Error al tomar foto", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // ----- ImageProxy helpers -----
    private fun ImageProxy.toBitmap(): Bitmap? {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun ImageProxy.toBitmapWithRotation(isFrontCamera: Boolean = false): Bitmap? {
        val bitmap = toBitmap() ?: return null
        val matrix = Matrix()
        matrix.postRotate(imageInfo.rotationDegrees.toFloat())
        if (isFrontCamera) matrix.postScale(-1f, 1f) // espejo horizontal
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // ----- Filtros -----
    private fun applyFilter(bitmap: Bitmap, filter: String): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()
        val matrix = ColorMatrix()

        when (filter) {
            "Grayscale" -> matrix.setSaturation(0f)

            "Sepia" -> matrix.set(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f,     0f,     0f,    1f, 0f
                )
            )

            "Brillo" -> matrix.set(
                floatArrayOf(
                    1.5f, 0f,   0f,   0f, 0f,
                    0f,   1.5f, 0f,   0f, 0f,
                    0f,   0f,   1.5f, 0f, 0f,
                    0f,   0f,   0f,   1f, 0f
                )
            )

            "Contraste" -> {
                // Contraste simple: factor c > 1 incrementa contraste; 1.0 = sin cambio
                val c = 1.3f
                val t = 128f * (1f - c) // traslación para mantener el punto medio
                matrix.set(
                    floatArrayOf(
                        c,   0f,  0f, 0f, t,
                        0f,  c,   0f, 0f, t,
                        0f,  0f,  c,  0f, t,
                        0f,  0f,  0f, 1f, 0f
                    )
                )
            }

            else -> {
                // Ninguno -> identidad
                matrix.reset()
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(result, 0f, 0f, paint)
        return result
    }

    // ----- Guardado con opción de formato -----
    private fun saveBitmap(bitmap: Bitmap, name: String, format: String): android.net.Uri? {
        val isPng = format.equals("PNG", ignoreCase = true)
        val mime = if (isPng) "image/png" else "image/jpeg"
        val displayName = "IMG_$name" + if (isPng) ".png" else ".jpg"

        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
            }
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        try {
            // 1) Escribir la imagen en el formato elegido
            resolver.openOutputStream(imageUri)?.use { out ->
                val ok = if (isPng) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                if (!ok) throw RuntimeException("Falló compresión $format")
            }

            // 2) EXIF solo para JPEG
            if (!isPng) {
                resolver.openFileDescriptor(imageUri, "rw")?.use { pfd ->
                    val exif = ExifInterface(pfd.fileDescriptor)
                    val now = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date())
                    exif.setAttribute(ExifInterface.TAG_DATETIME, now)
                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, now)
                    exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, now)
                    exif.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER ?: "Android")
                    exif.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL ?: "Device")
                    exif.setAttribute(ExifInterface.TAG_SOFTWARE, "Practica3-App")
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
                    exif.saveAttributes()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { resolver.delete(imageUri, null, null) } catch (_: Exception) {}
            return null
        }

        return imageUri
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permisos necesarios para usar cámara", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
