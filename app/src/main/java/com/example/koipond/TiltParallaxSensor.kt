package com.example.koipond

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Shared low-power 3D window parallax controller used by both the live wallpaper Engine
 * and the in-app preview View (eliminates duplicated sensor fusion math).
 * - Prefers `TYPE_GAME_ROTATION_VECTOR` (gyro+accel fusion, zero magnetometer power draw)
 *   with `SENSOR_DELAY_UI` + 40ms hardware FIFO batching.
 * - Slowly adapts its neutral baseline so whatever posture you hold your phone in stays centered.
 */
class TiltParallaxSensor(context: Context) : SensorEventListener {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val gyroFallbackSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var isRegistered = false
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var hasNeutralBaseline = false
    private var neutralPitch = 0f
    private var neutralRoll = 0f

    @Volatile
    var tiltX: Float = 0f
        private set

    @Volatile
    var tiltY: Float = 0f
        private set

    fun setActive(active: Boolean) {
        val chosen = rotationSensor ?: gyroFallbackSensor
        if (active && !isRegistered && chosen != null) {
            hasNeutralBaseline = false
            sensorManager?.registerListener(this, chosen, SensorManager.SENSOR_DELAY_UI, 40_000)
            isRegistered = true
        } else if (!active && isRegistered) {
            unregister()
        }
    }

    fun unregister() {
        if (isRegistered) {
            sensorManager?.unregisterListener(this)
            isRegistered = false
            hasNeutralBaseline = false
            tiltX = 0f
            tiltY = 0f
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val pitch = orientationAngles[1]
                val roll = orientationAngles[2]
                if (!hasNeutralBaseline) {
                    neutralPitch = pitch
                    neutralRoll = roll
                    hasNeutralBaseline = true
                } else {
                    neutralPitch += (pitch - neutralPitch) * 0.012f
                    neutralRoll += (roll - neutralRoll) * 0.012f
                }
                val targetX = ((roll - neutralRoll) * 2.1f).coerceIn(-1.25f, 1.25f)
                val targetY = ((pitch - neutralPitch) * 2.1f).coerceIn(-1.25f, 1.25f)
                tiltX += (targetX - tiltX) * 0.22f
                tiltY += (targetY - tiltY) * 0.22f
            }
            Sensor.TYPE_GYROSCOPE -> {
                tiltX = (tiltX * 0.92f + event.values[1] * 0.16f).coerceIn(-1.25f, 1.25f)
                tiltY = (tiltY * 0.92f + event.values[0] * 0.16f).coerceIn(-1.25f, 1.25f)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

fun Context.isSystemNightMode(): Boolean {
    val uiMask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMask == Configuration.UI_MODE_NIGHT_YES
}
