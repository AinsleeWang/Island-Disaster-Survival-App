package com.example.islanddisastersurvivalguideapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class NavigationSensorManager(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // 感測器
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    // 優先使用硬體計步器
    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // 數據緩衝 (加入 Low-Pass Filter 需要的變數)
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // 【面試考點】濾波係數 (Alpha)
    // 0.97 代表：新的數值 = 97% 的舊數值 + 3% 的新數值
    // 這樣可以過濾掉手抖的高頻雜訊
    private val ALPHA = 0.97f

    // 步數檢測
    private var stepCount = 0

    // 回調介面
    private var stepListener: ((Int) -> Unit)? = null
    private var directionListener: ((Float) -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    // 套用低通濾波
                    gravity = lowPass(event.values.clone(), gravity)
                    updateOrientationAngles()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    // 套用低通濾波
                    geomagnetic = lowPass(event.values.clone(), geomagnetic)
                    updateOrientationAngles()
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    // 如果手機有硬體計步器，直接用這個，最準最省電
                    if (event.values[0] == 1.0f) {
                        onStepDetected()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 面試可提：如果精度變低 (Unreliable)，可提示用戶畫 8 字校正
        }
    }

    // 【面試考點】低通濾波演算法實作
    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] * ALPHA + input[i] * (1 - ALPHA)
        }
        return output
    }

    private fun onStepDetected() {
        stepCount++
        stepListener?.invoke(stepCount)
    }

    private fun updateOrientationAngles() {
        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)

            // 傳感器融合 (Sensor Fusion): 結合加速度與磁力算出旋轉矩陣
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                // 將弧度轉為角度 (0~360度)
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f

                directionListener?.invoke(azimuth)
            }
        }
    }

    fun startListening() {
        stepCount = 0
        // SENSOR_DELAY_GAME 適合導航，延遲適中且流暢
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        stepDetector?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    fun setStepListener(listener: (Int) -> Unit) {
        stepListener = listener
    }

    fun setDirectionListener(listener: (Float) -> Unit) {
        directionListener = listener
    }
}