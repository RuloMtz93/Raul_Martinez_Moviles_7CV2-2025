package com.example.practica3_aplicacion_movil_nativa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.Locale

class PhotoDetailActivity : BaseActivity() {

    private var lastUriString: String? = null
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        val photoView: PhotoView = findViewById(R.id.photoView)
        val btnEdit: Button = findViewById(R.id.btnEdit)
        val btnInfo: Button = findViewById(R.id.btnInfo)
        val btnShare: Button = findViewById(R.id.btnShare)

        val uriString = intent.getStringExtra("photoUri")
        lastUriString = uriString

        if (uriString.isNullOrEmpty()) {
            Toast.makeText(this, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show()
            Log.e("PhotoDetail", "photoUri extra es null o vacío")
            finish()
            return
        }

        photoUri = try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            Toast.makeText(this, "URI inválida", Toast.LENGTH_SHORT).show()
            Log.e("PhotoDetail", "Error parseando URI: ${e.message}")
            finish()
            return
        }

        // Carga de imagen
        Glide.with(this)
            .load(photoUri)
            .fitCenter()
            .into(photoView)

        // Editar
        btnEdit.setOnClickListener {
            val i = Intent(this, ImageEditorActivity::class.java)
            i.putExtra("photoUri", lastUriString)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(i)
        }

        // Info EXIF
        btnInfo.setOnClickListener {
            showExifDialog(photoUri)
        }

        // Compartir
        btnShare.setOnClickListener {
            shareImage(photoUri)
        }
    }

    private fun shareImage(uri: Uri) {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(i, "Compartir imagen"))
    }

    private fun showExifDialog(uri: Uri) {
        // Abrimos por stream (válido para content:// de MediaStore)
        val exif = try {
            contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
        } catch (e: Exception) {
            null
        }

        if (exif == null) {
            AlertDialog.Builder(this)
                .setTitle("Metadatos EXIF")
                .setMessage("No se pudieron obtener metadatos.")
                .setPositiveButton("Cerrar", null)
                .show()
            return
        }

        val sb = SpannableStringBuilder()

        fun add(label: String, value: String?) {
            if (!value.isNullOrBlank()) {
                sb.append("• ").append(label).append(": ").append(value).append("\n")
            }
        }

        // Fecha/hora
        val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
        add("Fecha", dateTime?.let { exifDateToHuman(it) } ?: "—")

        // Cámara
        add("Marca", exif.getAttribute(ExifInterface.TAG_MAKE) ?: "—")
        add("Modelo", exif.getAttribute(ExifInterface.TAG_MODEL) ?: "—")

        // Dimensiones
        val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)
        val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1)
        if (width > 0 && height > 0) add("Resolución", "${width}×${height}px")

        // Exposición
        exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let {
            add("Exposición", formatExposure(it))
        }
        // Apertura
        exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, -1.0).let {
            if (it > 0) add("Apertura", "f/${trimZeros(it)}")
        }
        // ISO
        val iso = exif.getAttributeInt(ExifInterface.TAG_ISO_SPEED_RATINGS, -1)
        val isoAlt = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, -1)
        when {
            iso > 0 -> add("ISO", iso.toString())
            isoAlt > 0 -> add("ISO", isoAlt.toString())
        }
        // Longitud focal
        exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let {
            add("Focal", formatRationalMm(it))
        }

        // GPS
        val latLong = FloatArray(2)
        if (exif.getLatLong(latLong)) {
            add("Ubicación", "${latLong[0]}, ${latLong[1]}")
        }

        if (sb.isEmpty()) sb.append("Sin metadatos EXIF relevantes.")

        AlertDialog.Builder(this)
            .setTitle("Metadatos EXIF")
            .setMessage(sb)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    // ---------- Helpers de formato ----------
    private fun exifDateToHuman(exifDate: String): String {
        // Formato típico: "yyyy:MM:dd HH:mm:ss"
        return try {
            val inFmt = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
            val outFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            outFmt.format(inFmt.parse(exifDate)!!)
        } catch (e: Exception) {
            exifDate
        }
    }

    private fun trimZeros(d: Double): String {
        val s = String.format(Locale.US, "%.2f", d)
        return s.replace(Regex("[.,]00$"), "")
            .replace(Regex("0$"), "")
    }

    private fun formatExposure(raw: String): String {
        // Si viene como "0.005", mostrar "1/200 s" aprox
        return try {
            val v = raw.toDouble()
            if (v <= 0) raw
            else {
                val inv = (1.0 / v).toInt()
                when {
                    inv >= 2 && inv <= 8000 -> "1/$inv s"
                    else -> "${trimZeros(v)} s"
                }
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun formatRationalMm(rational: String): String {
        // Ej: "35/1" -> "35 mm"
        return try {
            val parts = rational.split("/")
            if (parts.size == 2) {
                val num = parts[0].toDouble()
                val den = parts[1].toDouble()
                val mm = num / den
                "${trimZeros(mm)} mm"
            } else rational
        } catch (_: Exception) {
            rational
        }
    }
}
