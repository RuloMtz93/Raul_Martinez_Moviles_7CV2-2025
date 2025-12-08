package com.example.conecta4.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.conecta4.data.save.SaveFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("conecta4_prefs")

enum class ThemeBrand { GUINDA, AZUL }
enum class DarkMode { SYSTEM, LIGHT, DARK }
enum class AiDifficulty { EASY, MEDIUM, HARD }

data class AppPrefs(
    val defaultFormat: SaveFormat = SaveFormat.JSON,
    val themeBrand: ThemeBrand = ThemeBrand.GUINDA,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM
)

object PrefsRepo {

    private lateinit var appCtx: Context

    private val KEY_DEFAULT_FORMAT = stringPreferencesKey("default_format")
    private val KEY_THEME_BRAND    = stringPreferencesKey("theme_brand")
    private val KEY_DARK_MODE      = stringPreferencesKey("dark_mode")
    private val KEY_SOUND          = booleanPreferencesKey("sound_enabled")
    private val KEY_AI_DIFF        = stringPreferencesKey("ai_difficulty")

    fun init(ctx: Context) { appCtx = ctx.applicationContext }

    fun observe(): Flow<AppPrefs> = appCtx.dataStore.data.map { p ->
        AppPrefs(
            defaultFormat = p[KEY_DEFAULT_FORMAT]?.let { runCatching { SaveFormat.valueOf(it) }.getOrNull() } ?: SaveFormat.JSON,
            themeBrand    = p[KEY_THEME_BRAND]?.let { runCatching { ThemeBrand.valueOf(it) }.getOrNull() } ?: ThemeBrand.GUINDA,
            darkMode      = p[KEY_DARK_MODE]?.let { runCatching { DarkMode.valueOf(it) }.getOrNull() } ?: DarkMode.SYSTEM,
            soundEnabled  = p[KEY_SOUND] ?: true,
            aiDifficulty  = p[KEY_AI_DIFF]?.let { runCatching { AiDifficulty.valueOf(it) }.getOrNull() } ?: AiDifficulty.MEDIUM
        )
    }

    suspend fun setDefaultFormat(f: SaveFormat) {
        appCtx.dataStore.edit { it[KEY_DEFAULT_FORMAT] = f.name }
    }
    suspend fun setThemeBrand(b: ThemeBrand) {
        appCtx.dataStore.edit { it[KEY_THEME_BRAND] = b.name }
    }
    suspend fun setDarkMode(m: DarkMode) {
        appCtx.dataStore.edit { it[KEY_DARK_MODE] = m.name }
    }
    suspend fun setSoundEnabled(on: Boolean) {
        appCtx.dataStore.edit { it[KEY_SOUND] = on }
    }
    suspend fun setAiDifficulty(d: AiDifficulty) {
        appCtx.dataStore.edit { it[KEY_AI_DIFF] = d.name }
    }
}
