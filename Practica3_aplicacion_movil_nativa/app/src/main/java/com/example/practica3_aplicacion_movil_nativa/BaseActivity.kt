package com.example.practica3_aplicacion_movil_nativa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private var themeVersionAtCreate: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)          // aplica overlay + night
        themeVersionAtCreate = ThemeHelper.getVersion(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val current = ThemeHelper.getVersion(this)
        if (current != themeVersionAtCreate) {
            // El tema cambió mientras esta Activity estaba en segundo plano -> recrear
            themeVersionAtCreate = current
            recreate()
        }
    }
}
