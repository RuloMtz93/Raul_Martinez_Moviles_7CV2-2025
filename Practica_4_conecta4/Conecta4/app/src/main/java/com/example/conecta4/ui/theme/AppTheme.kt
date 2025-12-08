package com.example.conecta4.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.conecta4.data.prefs.DarkMode
import com.example.conecta4.data.prefs.ThemeBrand

// GUINDA (IPN)
private val GuindaLight = lightColorScheme(
    primary = Color(0xFF7B1F3A),
    onPrimary = Color.White,
    secondary = Color(0xFFB23A48),
    onSecondary = Color.White,
    tertiary = Color(0xFF8E3351),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F)
)
private val GuindaDark = darkColorScheme(
    primary = Color(0xFFF0BBD0),
    onPrimary = Color(0xFF4A0F22),
    secondary = Color(0xFFE4A2AE),
    onSecondary = Color(0xFF3F0C17),
    tertiary = Color(0xFFE1A8C0),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

// AZUL (ESCOM)
private val AzulLight = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    secondary = Color(0xFF1565C0),
    onSecondary = Color.White,
    tertiary = Color(0xFF1976D2),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F)
)
private val AzulDark = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF002F6C),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF003C8F),
    tertiary = Color(0xFF64B5F6),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun ConectaTheme(
    brand: ThemeBrand,
    darkMode: DarkMode,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current

    val useDark = when (darkMode) {
        DarkMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        DarkMode.LIGHT  -> false
        DarkMode.DARK   -> true
    }

    val colorScheme: ColorScheme = when (brand) {
        ThemeBrand.GUINDA -> if (useDark) GuindaDark else GuindaLight
        ThemeBrand.AZUL   -> if (useDark) AzulDark else AzulLight
    }

    // Si quieres soporte de colores dinámicos (Android 12+), descomenta:
    // val dynamic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    //     if (useDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    // } else null
    // val cs = dynamic ?: colorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
