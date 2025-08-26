package com.prism.app.sensors

import android.content.Context
import android.hardware.*
import kotlin.math.sqrt

class SensorCollector(private val ctx: Context, private val listener: Listener) : SensorEventListener {
    interface Listener {
        fun onStepDetected(count: Int, strideMeters: Float)
        fun onHeadingUpdated(deg: Float)
        fun onSensorRaw(ts: Long, ax: Float, ay: Float, az: Float, gx: Float, gy: Float, gz: Float)
    }

    private val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var stepCount = 0

    fun start() {
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_UI)
        sm.registerListener(this, rot, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() { sm.unregisterListener(this) }

    override fun onSensorChanged(event: SensorEvent) {
        val ts = System.currentTimeMillis()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]; val ay = event.values[1]; val az = event.values[2]
                // simple peak-based step detection (very naive)
                val mag = sqrt(ax*ax + ay*ay + az*az)
                if (mag > 13f) { // threshold - tune in real app
                    stepCount++
                    val stride = 0.7f // fallback average stride
                    listener.onStepDetected(stepCount, stride)
                }
                listener.onSensorRaw(ts, ax, ay, az, 0f,0f,0f)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]; val gy = event.values[1]; val gz = event.values[2]
                listener.onSensorRaw(ts, 0f,0f,0f, gx,gy,gz)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotMat = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotMat, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotMat, orientation)
                val headingRad = orientation[0]
                val headingDeg = Math.toDegrees(headingRad.toDouble()).toFloat()
                listener.onHeadingUpdated((headingDeg + 360f) % 360f)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}
