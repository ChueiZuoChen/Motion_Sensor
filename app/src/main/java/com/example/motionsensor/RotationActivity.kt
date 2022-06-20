package com.example.motionsensor

import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.motionsensor.databinding.ActivityRotationBinding

private const val TAG = "RotationActivity"
class RotationActivity : AppCompatActivity() ,SensorEventListener{
    lateinit var binding: ActivityRotationBinding
    lateinit var sensorManager: SensorManager
    lateinit var sensor: Sensor
    var mx = 0.0
    var my = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
                Log.d(TAG, String.format("%.2f\t%.2f\t%.2f",it.values[0],it.values[1],it.values[2],))
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this,sensor,Sensor.TYPE_ROTATION_VECTOR)
    }
}