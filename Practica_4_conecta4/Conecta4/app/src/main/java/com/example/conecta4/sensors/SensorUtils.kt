package com.example.conecta4.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Recuerda: estos sensores NO requieren permisos en tiempo de ejecución.
 * Si el dispositivo no tiene el sensor, devolvemos un valor seguro (false/null).
 */

/* ------------------------------
   PROXIMIDAD: rememberProximityState
   - true: objeto MUY CERCA del sensor
   - false: lejos o no disponible
   ------------------------------ */
@Composable
fun rememberProximityState(): State<Boolean> {
    val context = LocalContext.current
    val isNear = remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
                    val distance = event.values.firstOrNull() ?: return
                    // Regla general: si distance < maxRange => “cerca”
                    isNear.value = sensor != null && distance < (sensor.maximumRange.coerceAtLeast(1f))
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            isNear.value = false
        }

        onDispose {
            if (sensor != null) {
                sm.unregisterListener(listener)
            }
        }
    }

    return isNear
}

/* -------------------------------------
   LUZ AMBIENTAL: rememberAmbientLuxState
   - valor en lux (Float) o null si no hay sensor
   ------------------------------------- */
@Composable
fun rememberAmbientLuxState(): State<Float?> {
    val context = LocalContext.current
    val lux = remember { mutableStateOf<Float?>(null) }

    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    lux.value = event.values.firstOrNull()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensor != null) {
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            lux.value = null
        }

        onDispose {
            if (sensor != null) {
                sm.unregisterListener(listener)
            }
        }
    }

    return lux
}