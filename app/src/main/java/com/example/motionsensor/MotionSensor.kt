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
import kotlin.math.abs
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
    var xRotateDegree: Double = 0.0
    var zRotateDegree: Double = 0.0
    var isInZRotateRange: Boolean = false
    var isInXRotateRange: Boolean = false

    init {
        isInXRotateRange = false
        isInZRotateRange = false
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
                    xRotateDegree = orientation[1] * FROM_RADES_TO_DEGS
                    zRotateDegree = orientation[2] * FROM_RADES_TO_DEGS
                    over = xRotateDegree > 0.0
                }
            }
        }
        circleMove.layoutParams = params
        if (1.5 > abs(zRotateDegree)) {
            if (!isInXRotateRange) {
                isInXRotateRange = true
            }
        } else {
            if (isInZRotateRange) {
                isInXRotateRange = false
            }
        }
        if (1.5 > abs(xRotateDegree)) {
            if (!isInZRotateRange) {
                isInZRotateRange = true
                callback.isWrongOrientation(isInZRotateRange, isInXRotateRange)
            }
        } else {
            if (isInZRotateRange) {
                isInZRotateRange = false
                callback.isWrongOrientation(isInZRotateRange, isInXRotateRange)
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
    fun isWrongOrientation(isInZRotateRange: Boolean, isInXRotateRange: Boolean): Boolean
}