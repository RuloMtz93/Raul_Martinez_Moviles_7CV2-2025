package com.example.practica3_aplicacion_movil_nativa

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val PREFS = "app_theme_prefs"
    private const val KEY_THEME = "theme"       // "guinda" | "azul"
    private const val KEY_NIGHT = "night_mode"  // "system" | "light" | "dark"
    private const val KEY_VERSION = "theme_version" // incrementa cuando cambie algo

    enum class AppTheme(val key: String) { GUINDA("guinda"), AZUL("azul") }
    enum class Night(val key: String) { SYSTEM("system"), LIGHT("light"), DARK("dark") }

    fun getTheme(ctx: Context): AppTheme {
        val v = prefs(ctx).getString(KEY_THEME, AppTheme.GUINDA.key) ?: AppTheme.GUINDA.key
        return if (v == AppTheme.AZUL.key) AppTheme.AZUL else AppTheme.GUINDA
    }

    fun getNight(ctx: Context): Night {
        return when (prefs(ctx).getString(KEY_NIGHT, Night.SYSTEM.key)) {
            Night.LIGHT.key -> Night.LIGHT
            Night.DARK.key -> Night.DARK
            else -> Night.SYSTEM
        }
    }

    fun getVersion(ctx: Context): Int = prefs(ctx).getInt(KEY_VERSION, 0)

    fun setTheme(ctx: Context, theme: AppTheme) {
        prefs(ctx).edit().putString(KEY_THEME, theme.key).apply()
        bumpVersion(ctx)
    }

    fun setNight(ctx: Context, night: Night) {
        prefs(ctx).edit().putString(KEY_NIGHT, night.key).apply()
        AppCompatDelegate.setDefaultNightMode(
            when (night) {
                Night.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Night.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                Night.DARK  -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
        bumpVersion(ctx)
    }

    /** Aplícalo al principio de cada Activity. */
    fun applyTheme(activity: Activity) {
        // Reafirma night mode actual
        when (getNight(activity)) {
            Night.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            Night.LIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Night.DARK   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        // Overlay de colores
        val overlay = when (getTheme(activity)) {
            AppTheme.GUINDA -> R.style.ThemeOverlay_Guinda
            AppTheme.AZUL   -> R.style.ThemeOverlay_Azul
        }
        activity.theme.applyStyle(overlay, true)
    }

    private fun bumpVersion(ctx: Context) {
        val p = prefs(ctx)
        val v = p.getInt(KEY_VERSION, 0) + 1
        p.edit().putInt(KEY_VERSION, v).apply()
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
