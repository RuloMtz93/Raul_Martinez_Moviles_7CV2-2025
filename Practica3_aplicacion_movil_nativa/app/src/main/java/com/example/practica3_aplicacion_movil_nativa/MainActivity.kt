package com.example.practica3_aplicacion_movil_nativa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : BaseActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    private lateinit var btnCamera: Button
    private lateinit var btnAudio: Button
    private lateinit var btnPhotoGallery: Button
    private lateinit var btnAudioGallery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSettings: Button = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnCamera = findViewById(R.id.btnCamera)
        btnAudio = findViewById(R.id.btnAudio)
        btnPhotoGallery = findViewById(R.id.btnPhotoGallery)
        btnAudioGallery = findViewById(R.id.btnAudioGallery)

        checkPermissions()

        btnCamera.setOnClickListener { startActivity(Intent(this, CameraActivity::class.java)) }
        btnAudio.setOnClickListener { startActivity(Intent(this, AudioRecorderActivity::class.java)) }
        btnPhotoGallery.setOnClickListener { startActivity(Intent(this, PhotoGalleryActivity::class.java)) }
        btnAudioGallery.setOnClickListener { startActivity(Intent(this, AudioGalleryActivity::class.java)) }
    }

    private fun checkPermissions() {
        val toRequest = mutableListOf<String>()

        if (!isGranted(Manifest.permission.CAMERA)) toRequest.add(Manifest.permission.CAMERA)
        if (!isGranted(Manifest.permission.RECORD_AUDIO)) toRequest.add(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isGranted(Manifest.permission.READ_MEDIA_AUDIO)) toRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            if (!isGranted(Manifest.permission.READ_MEDIA_IMAGES)) toRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (!isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) toRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                toRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val denied = permissions.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Permisos denegados: ${denied.joinToString { permissions[it].substringAfterLast('.') }}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
