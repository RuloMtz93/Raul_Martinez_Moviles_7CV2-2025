package com.example.practica3_aplicacion_movil_nativa

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : BaseActivity() {

    private lateinit var rgTheme: RadioGroup
    private lateinit var rbGuinda: RadioButton
    private lateinit var rbAzul: RadioButton

    private lateinit var rgNight: RadioGroup
    private lateinit var rbSystem: RadioButton
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica tema seleccionado
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        rgTheme = findViewById(R.id.rgTheme)
        rbGuinda = findViewById(R.id.rbGuinda)
        rbAzul = findViewById(R.id.rbAzul)

        rgNight = findViewById(R.id.rgNight)
        rbSystem = findViewById(R.id.rbSystem)
        rbLight = findViewById(R.id.rbLight)
        rbDark = findViewById(R.id.rbDark)

        // Cargar estado
        when (ThemeHelper.getTheme(this)) {
            ThemeHelper.AppTheme.GUINDA -> rbGuinda.isChecked = true
            ThemeHelper.AppTheme.AZUL -> rbAzul.isChecked = true
        }
        when (ThemeHelper.getNight(this)) {
            ThemeHelper.Night.SYSTEM -> rbSystem.isChecked = true
            ThemeHelper.Night.LIGHT -> rbLight.isChecked = true
            ThemeHelper.Night.DARK -> rbDark.isChecked = true
        }

        // Cambios de theme
        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val t = if (checkedId == rbAzul.id) ThemeHelper.AppTheme.AZUL else ThemeHelper.AppTheme.GUINDA
            ThemeHelper.setTheme(this, t)
            ThemeHelper.applyTheme(this)
            recreate()
            Toast.makeText(this, "Tema aplicado", Toast.LENGTH_SHORT).show()
        }

        // Cambios de modo
        rgNight.setOnCheckedChangeListener { _, checkedId ->
            val n = when (checkedId) {
                rbLight.id -> ThemeHelper.Night.LIGHT
                rbDark.id -> ThemeHelper.Night.DARK
                else -> ThemeHelper.Night.SYSTEM
            }
            ThemeHelper.setNight(this, n)
            recreate()
        }
    }
}
