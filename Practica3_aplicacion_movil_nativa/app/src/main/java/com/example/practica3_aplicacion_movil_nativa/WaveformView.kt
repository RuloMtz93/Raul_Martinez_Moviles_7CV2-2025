package com.example.practica3_aplicacion_movil_nativa

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.max

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
    }

    private var waveform: ByteArray? = null

    fun setWaveform(bytes: ByteArray) {
        waveform = bytes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // Fondo (opcional, se hereda del tema)
        // canvas.drawColor(Color.TRANSPARENT)

        val data = waveform ?: return
        if (data.isEmpty()) return

        val centerY = h / 2f
        val step = max(1, data.size / (width / 3)) // muestra barras separadas

        // color primario del tema
        paint.color = resolveAttrColor(androidx.appcompat.R.attr.colorPrimary)

        var x = 0f
        val dx = 3f

        for (i in 0 until data.size step step) {
            val v = data[i]
            // normalizar byte (-128..127) a altura
            val amplitude = (abs(v.toInt()) / 128f) * (h * 0.9f) / 2f
            canvas.drawLine(x, centerY - amplitude, x, centerY + amplitude, paint)
            x += dx
            if (x > w) break
        }
    }

    private fun resolveAttrColor(attr: Int): Int {
        val a = context.obtainStyledAttributes(intArrayOf(attr))
        val color = a.getColor(0, 0xFF777777.toInt())
        a.recycle()
        return color
    }
}
