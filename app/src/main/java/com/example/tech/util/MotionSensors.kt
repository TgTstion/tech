package com.example.tech.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Pure-Kotlin gesture logic (no Android types), so it can be unit-tested.
 * Portrait: tilting the RIGHT edge down makes x negative; the LEFT edge down makes x positive.
 */
class TiltShakeDetector(
    private val tiltThreshold: Float = 5f,      // m/s²
    private val neutralThreshold: Float = 2f,   // m/s² — must return here before the next tilt
    private val shakeThresholdG: Float = 2.5f,  // multiples of gravity
    private val cooldownMs: Long = 700,
) {
    enum class Gesture { TILT_RIGHT, TILT_LEFT, SHAKE }

    private var armed = true
    private var lastGestureAt = 0L

    fun process(x: Float, y: Float, z: Float, nowMs: Long): Gesture? {
        if (nowMs - lastGestureAt < cooldownMs) return null

        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        if (gForce > shakeThresholdG) return fire(Gesture.SHAKE, nowMs)

        if (abs(x) < neutralThreshold) {
            armed = true
            return null
        }
        if (!armed) return null
        return when {
            x < -tiltThreshold -> fire(Gesture.TILT_RIGHT, nowMs)
            x > tiltThreshold -> fire(Gesture.TILT_LEFT, nowMs)
            else -> null
        }
    }

    private fun fire(g: Gesture, now: Long): Gesture {
        lastGestureAt = now
        armed = false
        return g
    }
}

/**
 * Registers the accelerometer only while this composable is on screen and [enabled] is true;
 * onDispose unregisters it, so no sensor runs in the background.
 */
@Composable
fun MotionControls(
    enabled: Boolean,
    onTiltRight: () -> Unit,
    onTiltLeft: () -> Unit,
    onShake: () -> Unit,
) {
    val context = LocalContext.current
    val right by rememberUpdatedState(onTiltRight)
    val left by rememberUpdatedState(onTiltLeft)
    val shake by rememberUpdatedState(onShake)

    DisposableEffect(enabled) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = TiltShakeDetector()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = Triple(event.values[0], event.values[1], event.values[2])
                when (detector.process(x, y, z, SystemClock.elapsedRealtime())) {
                    TiltShakeDetector.Gesture.TILT_RIGHT -> right()
                    TiltShakeDetector.Gesture.TILT_LEFT -> left()
                    TiltShakeDetector.Gesture.SHAKE -> shake()
                    null -> Unit
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (enabled && accelerometer != null) {
            sm.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose { sm.unregisterListener(listener) }
    }
}

/**
 * Proximity sensor: fires [onNear] once each time the phone is brought to the ear.
 * Most devices report a binary near/far value, so "near" = below maximumRange (capped at 5 cm).
 */
@Composable
fun RaiseToEar(enabled: Boolean, onNear: () -> Unit) {
    val context = LocalContext.current
    val near by rememberUpdatedState(onNear)

    DisposableEffect(enabled) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximity = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        var wasNear = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val limit = min(proximity?.maximumRange ?: 5f, 5f)
                val isNear = event.values[0] < limit
                if (isNear && !wasNear) near()
                wasNear = isNear
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (enabled && proximity != null) {
            sm.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sm.unregisterListener(listener) }
    }
}
