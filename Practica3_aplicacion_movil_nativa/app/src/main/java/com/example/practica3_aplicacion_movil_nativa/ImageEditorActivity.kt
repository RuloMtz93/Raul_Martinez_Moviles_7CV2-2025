package com.example.practica3_aplicacion_movil_nativa

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.chrisbanes.photoview.PhotoView
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ImageEditorActivity : BaseActivity() {

    // UI
    private lateinit var photoView: PhotoView
    private lateinit var btnRotate90: Button
    private lateinit var btnCropVisible: Button
    private lateinit var btnReset: Button
    private lateinit var btnSave: Button
    private lateinit var sbBrightness: SeekBar
    private lateinit var sbContrast: SeekBar
    private lateinit var tvBrightness: TextView
    private lateinit var tvContrast: TextView

    // Rotación libre
    private lateinit var tvRotate: TextView
    private lateinit var sbRotate: SeekBar
    private lateinit var btnRotateLeft: Button
    private lateinit var btnRotateRight: Button
    private lateinit var btnApplyRotation: Button
    private lateinit var btnResetRotation: Button

    // Aspect ratio
    private lateinit var rgAspect: RadioGroup
    private lateinit var rbFree: RadioButton
    private lateinit var rb1x1: RadioButton
    private lateinit var rb4x3: RadioButton
    private lateinit var rb16x9: RadioButton

    // Estado
    private lateinit var photoUri: Uri
    private var baseBitmap: Bitmap? = null
    private var previewMatrix: ColorMatrix = ColorMatrix()
    private var pendingRotationDeg: Float = 0f

    enum class Aspect { FREE, R1x1, R4x3, R16x9 }
    private var currentAspect: Aspect = Aspect.FREE

    companion object { private const val REQ_READ_IMG = 501 }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_editor)

        // Bind
        photoView = findViewById(R.id.editorPhotoView)
        btnRotate90 = findViewById(R.id.btnRotate90)
        btnCropVisible = findViewById(R.id.btnCropVisible)
        btnReset = findViewById(R.id.btnReset)
        btnSave = findViewById(R.id.btnSave)
        sbBrightness = findViewById(R.id.sbBrightness)
        sbContrast = findViewById(R.id.sbContrast)
        tvBrightness = findViewById(R.id.tvBrightness)
        tvContrast = findViewById(R.id.tvContrast)

        tvRotate = findViewById(R.id.tvRotate)
        sbRotate = findViewById(R.id.sbRotate)
        btnRotateLeft = findViewById(R.id.btnRotateLeft)
        btnRotateRight = findViewById(R.id.btnRotateRight)
        btnApplyRotation = findViewById(R.id.btnApplyRotation)
        btnResetRotation = findViewById(R.id.btnResetRotation)

        rgAspect = findViewById(R.id.rgAspect)
        rbFree = findViewById(R.id.rbFree)
        rb1x1 = findViewById(R.id.rb1x1)
        rb4x3 = findViewById(R.id.rb4x3)
        rb16x9 = findViewById(R.id.rb16x9)

        // URI
        val uriStr = intent.getStringExtra("photoUri")
        if (uriStr.isNullOrBlank()) { Toast.makeText(this, "Imagen inválida", Toast.LENGTH_SHORT).show(); finish(); return }
        photoUri = Uri.parse(uriStr)

        // Permisos
        if (!hasReadImagesPermission()) {
            requestReadImagesPermission()
        } else {
            initAfterPermission()
        }
    }

    // ====== Permisos ======
    private fun hasReadImagesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestReadImagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQ_READ_IMG)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_READ_IMG)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_READ_IMG) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) initAfterPermission() else { Toast.makeText(this, "Permiso requerido", Toast.LENGTH_SHORT).show(); finish() }
        }
    }

    // ====== Inicializar controles y cargar imagen ======
    private fun initAfterPermission() {
        baseBitmap = decodeBitmapFromUri(photoUri, 3000, 3000)
        if (baseBitmap == null) { Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show(); finish(); return }
        photoView.setImageBitmap(baseBitmap)

        // Brillo/Contraste
        sbBrightness.max = 200; sbBrightness.progress = 100
        sbContrast.max = 200; sbContrast.progress = 100
        tvBrightness.text = "Brillo: 0"; tvContrast.text = "Contraste: 100%"
        sbBrightness.setOnSeekBarChangeListener(simpleChange {
            val b = sbBrightness.progress - 100
            tvBrightness.text = "Brillo: $b"; applyPreviewFilter()
        })
        sbContrast.setOnSeekBarChangeListener(simpleChange {
            val c = sbContrast.progress
            tvContrast.text = "Contraste: ${c}%"; applyPreviewFilter()
        })

        // Rotación libre (preview con rotation de la View)
        tvRotate.text = "Rotación: 0°"
        sbRotate.max = 360
        sbRotate.progress = 0
        sbRotate.setOnSeekBarChangeListener(simpleChange {
            pendingRotationDeg = normalizeDeg(sbRotate.progress.toFloat())
            tvRotate.text = "Rotación: ${pendingRotationDeg.roundToInt()}°"
            photoView.rotation = pendingRotationDeg
        })
        btnRotateLeft.setOnClickListener {
            pendingRotationDeg = normalizeDeg(pendingRotationDeg - 90f)
            syncRotationControls()
        }
        btnRotateRight.setOnClickListener {
            pendingRotationDeg = normalizeDeg(pendingRotationDeg + 90f)
            syncRotationControls()
        }
        btnResetRotation.setOnClickListener {
            pendingRotationDeg = 0f
            syncRotationControls()
        }
        btnApplyRotation.setOnClickListener {
            // Aplica al bitmap (commit) y limpia preview
            baseBitmap = baseBitmap?.let { rotateBitmap(it, pendingRotationDeg) }
            pendingRotationDeg = 0f
            photoView.rotation = 0f
            photoView.setImageBitmap(baseBitmap)
            resetZoomAndFilter()
            Toast.makeText(this, "Rotación aplicada", Toast.LENGTH_SHORT).show()
        }

        // Aspect ratio
        rgAspect.setOnCheckedChangeListener { _, checkedId ->
            currentAspect = when (checkedId) {
                rb1x1.id -> Aspect.R1x1
                rb4x3.id -> Aspect.R4x3
                rb16x9.id -> Aspect.R16x9
                else -> Aspect.FREE
            }
        }

        // Rotar 90° (commit directo)
        btnRotate90.setOnClickListener {
            baseBitmap = baseBitmap?.let { rotateBitmap(it, 90f) }
            photoView.setImageBitmap(baseBitmap)
            resetZoomAndFilter()
        }

        // Recortar con relación actual
        btnCropVisible.setOnClickListener {
            if (pendingRotationDeg != 0f) {
                Toast.makeText(this, "Primero aplica la rotación.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cropped = when (currentAspect) {
                Aspect.FREE -> cropVisibleRegion()
                Aspect.R1x1 -> cropVisibleRegionWithAspect(1f / 1f)
                Aspect.R4x3 -> cropVisibleRegionWithAspect(4f / 3f)
                Aspect.R16x9 -> cropVisibleRegionWithAspect(16f / 9f)
            }
            if (cropped != null) {
                baseBitmap = cropped
                photoView.setImageBitmap(baseBitmap)
                resetZoomAndFilter()
                Toast.makeText(this, "Recortado (${currentAspectText()})", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se pudo recortar", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset ajustes (no deshace recortes/rotaciones ya aplicados)
        btnReset.setOnClickListener {
            sbBrightness.progress = 100
            sbContrast.progress = 100
            tvBrightness.text = "Brillo: 0"
            tvContrast.text = "Contraste: 100%"
            applyPreviewFilter()
            photoView.setScale(1f, true)

            pendingRotationDeg = 0f
            syncRotationControls()
        }

        // Guardar
        btnSave.setOnClickListener {
            val result = renderFinalBitmap()
            if (result == null) { Toast.makeText(this, "No se pudo preparar la imagen", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val ok = saveToMediaStore(result)
            Toast.makeText(this, if (ok) "Guardado en Imágenes/Edited" else "Error al guardar", Toast.LENGTH_SHORT).show()
            if (ok) finish()
        }
    }

    private fun syncRotationControls() {
        tvRotate.text = "Rotación: ${pendingRotationDeg.roundToInt()}°"
        sbRotate.progress = normalizeDeg(pendingRotationDeg).roundToInt().coerceIn(0, 360)
        photoView.rotation = pendingRotationDeg
    }

    private fun normalizeDeg(d: Float): Float {
        var x = d % 360f
        if (x < 0) x += 360f
        // Mostrar alrededor de [-180, 180] en texto
        val show = if (x > 180f) x - 360f else x
        return show
    }

    // ====== Helpers SeekBar ======
    private fun simpleChange(onChanged: () -> Unit): SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { onChanged() }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

    // ====== Filtros preview ======
    private fun applyPreviewFilter() {
        val b = (sbBrightness.progress - 100) // -100..+100
        val cPercent = sbContrast.progress     // 0..200
        val c = cPercent / 100f                // 0..2.0
        val translate = 255f * (b / 100f)

        val cm = ColorMatrix(floatArrayOf(
            c, 0f, 0f, 0f, translate,
            0f, c, 0f, 0f, translate,
            0f, 0f, c, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        previewMatrix = cm
        photoView.colorFilter = ColorMatrixColorFilter(cm)
    }

    // ====== Edición geométrica ======
    private fun rotateBitmap(src: Bitmap, angle: Float): Bitmap {
        if (angle == 0f) return src
        val m = Matrix(); m.postRotate(angle)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    private fun cropVisibleRegion(): Bitmap? {
        val bmp = baseBitmap ?: return null
        val displayRect = photoView.displayRect ?: return null

        val viewW = photoView.width.toFloat()
        val viewH = photoView.height.toFloat()
        val drawableW = displayRect.width()
        val drawableH = displayRect.height()
        if (drawableW <= 0f || drawableH <= 0f) return null

        val leftRel = ((0f - displayRect.left).coerceAtLeast(0f)) / drawableW
        val topRel = ((0f - displayRect.top).coerceAtLeast(0f)) / drawableH
        val rightRel = ((viewW - displayRect.left).coerceAtLeast(0f).coerceAtMost(drawableW)) / drawableW
        val bottomRel = ((viewH - displayRect.top).coerceAtLeast(0f).coerceAtMost(drawableH)) / drawableH

        val x = (leftRel * bmp.width).toInt().coerceIn(0, bmp.width - 1)
        val y = (topRel * bmp.height).toInt().coerceIn(0, bmp.height - 1)
        val w = ((rightRel - leftRel) * bmp.width).toInt().coerceIn(1, bmp.width - x)
        val h = ((bottomRel - topRel) * bmp.height).toInt().coerceIn(1, bmp.height - y)

        return try { Bitmap.createBitmap(bmp, x, y, w, h) } catch (_: Exception) { null }
    }

    private fun cropVisibleRegionWithAspect(targetAspect: Float): Bitmap? {
        val bmp = baseBitmap ?: return null
        val displayRect = photoView.displayRect ?: return null

        val viewW = photoView.width.toFloat()
        val viewH = photoView.height.toFloat()
        val drawableW = displayRect.width()
        val drawableH = displayRect.height()
        if (drawableW <= 0f || drawableH <= 0f) return null

        // Visible en coords de vista
        val visLeft = max(0f, displayRect.left)
        val visTop = max(0f, displayRect.top)
        val visRight = min(viewW, displayRect.right)
        val visBottom = min(viewH, displayRect.bottom)
        val visW = (visRight - visLeft).coerceAtLeast(1f)
        val visH = (visBottom - visTop).coerceAtLeast(1f)
        val visAspect = visW / visH

        // Rect recorte (en vista) con el aspect deseado, centrado, dentro de visible
        var cropW: Float
        var cropH: Float
        if (visAspect >= targetAspect) {
            // Visible es "más ancho": ajusta por altura
            cropH = visH
            cropW = cropH * targetAspect
        } else {
            // Visible es "más alto": ajusta por ancho
            cropW = visW
            cropH = cropW / targetAspect
        }
        val cropLeftV = visLeft + (visW - cropW) / 2f
        val cropTopV = visTop + (visH - cropH) / 2f
        val cropRightV = cropLeftV + cropW
        val cropBottomV = cropTopV + cropH

        // Convertir rect de vista -> fracción del drawable
        val leftRel = (cropLeftV - displayRect.left) / drawableW
        val topRel = (cropTopV - displayRect.top) / drawableH
        val rightRel = (cropRightV - displayRect.left) / drawableW
        val bottomRel = (cropBottomV - displayRect.top) / drawableH

        // Transformar a píxeles del bitmap
        val x = (leftRel * bmp.width).toInt().coerceIn(0, bmp.width - 1)
        val y = (topRel * bmp.height).toInt().coerceIn(0, bmp.height - 1)
        val w = ((rightRel - leftRel) * bmp.width).toInt().coerceIn(1, bmp.width - x)
        val h = ((bottomRel - topRel) * bmp.height).toInt().coerceIn(1, bmp.height - y)

        return try { Bitmap.createBitmap(bmp, x, y, w, h) } catch (_: Exception) { null }
    }

    private fun currentAspectText(): String = when (currentAspect) {
        Aspect.FREE -> "Libre"
        Aspect.R1x1 -> "1:1"
        Aspect.R4x3 -> "4:3"
        Aspect.R16x9 -> "16:9"
    }

    // ====== Render final (aplica brillo/contraste) ======
    private fun renderFinalBitmap(): Bitmap? {
        val src = baseBitmap ?: return null
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(previewMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    // ====== Decode ======
    private fun decodeBitmapFromUri(uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        // Bounds
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        try { contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } }
        catch (_: Exception) { return null }
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null

        val inSample = calculateInSampleSize(opts.outWidth, opts.outHeight, maxWidth, maxHeight)
        val opts2 = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = inSample
        }
        return try { contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts2) } }
        catch (_: Exception) { null }
    }

    private fun calculateInSampleSize(w: Int, h: Int, reqW: Int, reqH: Int): Int {
        var inSample = 1
        if (h > reqH || w > reqW) {
            var halfH = h / 2
            var halfW = w / 2
            while ((halfH / inSample) >= reqH && (halfW / inSample) >= reqW) inSample *= 2
        }
        return max(1, inSample)
    }

    private fun resetZoomAndFilter() {
        photoView.setScale(1f, true)
        applyPreviewFilter()
        // Mantiene aspect y rotación aplicada
    }

    private fun saveToMediaStore(bitmap: Bitmap): Boolean {
        val name = "EDIT_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Edited")
            }
        }
        return try {
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            contentResolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out) } ?: return false
            true
        } catch (_: Exception) { false }
    }
}
