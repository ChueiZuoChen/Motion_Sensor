package com.example.motionsensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import android.widget.RelativeLayout
import kotlin.properties.Delegates

class MotionSensor(
    private val sensorManager: SensorManager,
    private val viewRoot: View,
    private val circleMove: View,
) : SensorEventListener {
    private var mx by Delegates.notNull<Double>()
    private var my by Delegates.notNull<Double>()
    private var over by Delegates.notNull<Boolean>()
    private var sensorGravity: Sensor? = null
    private var sensorRotation: Sensor? = null

    init {
        mx = 0.0
        my = 0.0
        over = false
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_GRAVITY -> {
                if (mx == 0.0) {
                    mx = (viewRoot.width - circleMove.width) / 30.0
                    my = (viewRoot.height - circleMove.height) / 20.0
                }
                event.let {
                    val params = circleMove.layoutParams as RelativeLayout.LayoutParams
                    params.leftMargin = (((15 - it.values[0]) * mx).toInt())
                    if (over) {
                        params.topMargin = (((10 - it.values[1] + 9.81) * my).toInt())
                    } else {
                        params.topMargin = (((10 + it.values[1] - 9.81) * my).toInt())

                    }
                    circleMove.layoutParams = params
                }
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                over = event.values[0] >= 0.69f
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    fun registerListeners() {
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorRotation, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }
}