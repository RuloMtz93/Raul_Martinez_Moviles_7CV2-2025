package com.example.practica3_aplicacion_movil_nativa

import android.media.MediaActionSound

object Sounds {
    private val mas by lazy { MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) } }

    fun shutter() {
        try { mas.play(MediaActionSound.SHUTTER_CLICK) } catch (_: Exception) { /* no-op */ }
    }
}
