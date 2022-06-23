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
    private val context: Context,
    private val callback: CircleInRangeListener,
) : SensorEventListener {
    private var mx by Delegates.notNull<Double>()
    private var my by Delegates.notNull<Double>()
    private val sensorManager: SensorManager
    private var sensorGravity: Sensor? = null
    private var sensorRotation: Sensor? = null
    private var sensorAccelerometer: Sensor? = null
    private val FROM_RADES_TO_DEGS = -57.5
    var vector1: Double = 0.0
    var vector2: Double = 0.0
    var isInZ:Boolean = false
    var isInX:Boolean = false
    init {
        isInX = false
        isInZ = false
        mx = 0.0
        my = 0.0
        sensorManager = context.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
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
//                        params.leftMargin = (((15 - it.values[0]) * mx).toInt())
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
                    Log.d(TAG, "vector1: $vector1")
//                    Log.d(TAG, String.format("pitch: %.3f\tyaw: %.3f", vector1, vector2))
                    over = vector1 > 0.0
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                Log.d(TAG, "mag: ${event.values[0]}\t${event.values[1]}\t${event.values[2]}")
            }
        }
        circleMove.layoutParams = params
        if (1.5 > vector2 && vector2 > -1.5) {
            if (!isInX) {
                isInX = true
            }
        }else{
            if (isInZ){
                isInX = false
            }
        }
        if (1.5 > vector1 && vector1 > -1.5) {
            if (!isInZ) {
                isInZ = true
                callback.inRangeCallback(isInZ,isInX)
            }
        } else {
            if (isInZ) {
                isInZ = false
                callback.inRangeCallback(isInZ,isInX)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    fun registerListeners() {
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorRotation, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }
}

interface CircleInRangeListener {
    fun inRangeCallback(isInZ: Boolean, isInX: Boolean)
}