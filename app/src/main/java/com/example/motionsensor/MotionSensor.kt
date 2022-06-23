package com.example.motionsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.properties.Delegates

private const val TAG = "MotionSensor"

class MotionSensor(
    private val viewRoot: View,
    private val circleMove: View,
    context: Context,
    private val callback: OrientationListener,
) : SensorEventListener {
    private var mx by Delegates.notNull<Double>()
    private var my by Delegates.notNull<Double>()
    private val sensorManager: SensorManager
    private var sensorGravity: Sensor? = null
    private var sensorRotation: Sensor? = null
    private val FROM_RADES_TO_DEGS = -57.5
    var vector1: Double = 0.0
    var vector2: Double = 0.0
    var isInZ: Boolean = false
    var isInX: Boolean = false

    init {
        isInX = false
        isInZ = false
        mx = 0.0
        my = 0.0
        sensorManager = context.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    var over = false
    override fun onSensorChanged(event: SensorEvent?) {
        val params = circleMove.layoutParams as RelativeLayout.LayoutParams
        when (event?.sensor?.type) {
            Sensor.TYPE_GRAVITY -> {
                if (mx == 0.0) {
                    mx = (viewRoot.width - circleMove.width) / 30.0
                    my = (viewRoot.height - circleMove.height) / 20.0
                }
                event.let {
                    val params = circleMove.layoutParams as RelativeLayout.LayoutParams
                    if (over) {
                        params.leftMargin = (mx * 15).toInt()
                        params.topMargin = (((10 - it.values[1] + 9.81) * my).toInt())
                        Log.d(TAG, "top: ${params.topMargin}")
                    } else {
                        params.leftMargin = (mx * 15).toInt()
                        params.topMargin = (((10 + it.values[1] - 9.81) * my).toInt())
                        Log.d(TAG, "top: ${params.topMargin}")
                    }
                }
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                if (event.values.size > 4) {
                    val truncatedRotationVector = FloatArray(4)
                    System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4)
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix,
                        truncatedRotationVector)
                    val worldAxisX = SensorManager.AXIS_X
                    val worldAxisZ = SensorManager.AXIS_Z
                    val adjustedRotationMatrix = FloatArray(9)
                    SensorManager.remapCoordinateSystem(rotationMatrix,
                        worldAxisX,
                        worldAxisZ,
                        adjustedRotationMatrix)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(adjustedRotationMatrix, orientation)
                    vector1 = orientation[1] * FROM_RADES_TO_DEGS
                    vector2 = orientation[2] * FROM_RADES_TO_DEGS
                    over = vector1 > 0.0
                }
            }
        }
        circleMove.layoutParams = params
        if (1.5 > vector2 && vector2 > -1.5) {
            if (!isInX) {
                isInX = true
            }
        } else {
            if (isInZ) {
                isInX = false
            }
        }
        if (1.5 > vector1 && vector1 > -1.5) {
            if (!isInZ) {
                isInZ = true
                callback.isWrongOrientation(isInZ, isInX)
            }
        } else {
            if (isInZ) {
                isInZ = false
                callback.isWrongOrientation(isInZ, isInX)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun registerMotionSensorListeners() {
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorRotation, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterMotionSensorListener() {
        sensorManager.unregisterListener(this)
    }
}

interface OrientationListener {
    fun isWrongOrientation(isInZ: Boolean, isInX: Boolean)
}