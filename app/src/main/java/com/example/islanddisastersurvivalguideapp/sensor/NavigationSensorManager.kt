package com.example.islanddisastersurvivalguideapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class NavigationSensorManager(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 感測器
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // 數據緩衝
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // 步數檢測
    private var lastStepTime = 0L
    private var stepCount = 0
    private val STEP_DELAY_MS = 500 // 最小步數間隔
    private val ACCELERATION_THRESHOLD = 10f // 加速度閾值

    private var stepListener: ((Int) -> Unit)? = null
    private var directionListener: ((Float) -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                    detectStep(event.values)
                    updateOrientationAngles()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    updateOrientationAngles()
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    onStepDetected()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                Log.w("Sensors", "Unreliable sensor accuracy")
            }
        }
    }

    private fun detectStep(acceleration: FloatArray) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStepTime < STEP_DELAY_MS) return

        // 計算合成加速度
        val magnitude = sqrt(
            acceleration[0] * acceleration[0] +
                    acceleration[1] * acceleration[1] +
                    acceleration[2] * acceleration[2]
        )

        // 檢測步伐
        if (magnitude > ACCELERATION_THRESHOLD) {
            onStepDetected()
            lastStepTime = currentTime
        }
    }

    private fun onStepDetected() {
        stepCount++
        stepListener?.invoke(stepCount)
    }

    private fun updateOrientationAngles() {
        if (SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
        ) {
            val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val normalizedDegrees = (degrees + 360) % 360

            directionListener?.invoke(normalizedDegrees)
        }
    }

    fun startListening() {
        stepCount = 0

        // 註冊所有需要的感測器
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        sensorManager.registerListener(
            sensorEventListener,
            magnetometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        stepDetector?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        stepCount = 0
    }

    fun setStepListener(listener: (Int) -> Unit) {
        stepListener = listener
    }

    fun setDirectionListener(listener: (Float) -> Unit) {
        directionListener = listener
    }
}