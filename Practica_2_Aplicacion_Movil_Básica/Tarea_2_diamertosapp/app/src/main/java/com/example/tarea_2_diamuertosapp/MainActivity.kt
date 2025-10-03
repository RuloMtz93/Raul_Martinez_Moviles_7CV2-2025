package com.example.tarea_2_diamuertosapp

import android.content.Context
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.tarea_2_diamuertosapp.fragments.AltaresFragment

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "theme_prefs"
    private val KEY_DARK_MODE = "dark_mode"

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🔹 Cargar preferencia antes de setContentView
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val darkMode = sharedPrefs.getBoolean(KEY_DARK_MODE, false)
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 Cargar fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, AltaresFragment())
                .commit()
        }

        // 🔹 Opcional: crear un switch para cambiar el tema dinámicamente
        // Lo agregaremos por código ya que no estaba en tu layout original
        val switchTema = Switch(this)
        switchTema.text = "Modo Oscuro"
        switchTema.isChecked = darkMode

        // Colocarlo programáticamente arriba a la derecha
        val layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.setMargins(16, 16, 16, 16)
        addContentView(switchTema, layoutParams)

        // Listener del Switch
        switchTema.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply()
            recreate() // reinicia la activity para aplicar el tema
        }
    }
}
