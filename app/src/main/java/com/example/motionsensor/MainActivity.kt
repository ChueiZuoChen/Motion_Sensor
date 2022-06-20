package com.example.motionsensor

import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_GRAVITY
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Size
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.motionsensor.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {
    var over: Boolean = false
    lateinit var binding: ActivityMainBinding
    lateinit var sensorManager: SensorManager
    lateinit var sensor: Sensor
    lateinit var sensorR: Sensor
    var mx: Double = 0.0
    var my: Double = 0.0
    lateinit var circleMove: ImageView
    lateinit var viewRoot: RelativeLayout
    lateinit var preview: Preview
    var selector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    var cameraProvider: ProcessCameraProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorR = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        viewRoot = binding.root
        circleMove = binding.circleMove
        preview = Preview.Builder()
            .setTargetResolution(Size(720, 1280))
            .build()
            .also {
                it.setSurfaceProvider(binding.preview.surfaceProvider)
            }

        cameraProvider = ProcessCameraProvider.getInstance(this).get()
        cameraProvider!!.bindToLifecycle(this, selector, preview)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorR, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        when (event?.sensor?.type) {
            TYPE_GRAVITY -> {
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
            TYPE_ROTATION_VECTOR -> {
                over = event.values[0] >= 0.69f
            }

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }


}