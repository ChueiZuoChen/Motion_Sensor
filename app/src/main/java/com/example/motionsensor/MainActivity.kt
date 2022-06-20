package com.example.motionsensor

import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_GRAVITY
import android.hardware.Sensor.TYPE_ROTATION_VECTOR
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

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var preview: Preview
    var selector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    var cameraProvider: ProcessCameraProvider? = null

    /*Sensor*/
    lateinit var sensorGravity: Sensor
    lateinit var sensorRotation: Sensor
    lateinit var sensorManager: SensorManager
    lateinit var motionSensor: MotionSensor

    /*SensorView*/
    lateinit var circleMove: ImageView
    lateinit var viewRoot: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR)
        /*sensor implementation*/
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorGravity = sensorManager.getDefaultSensor(TYPE_GRAVITY)
        sensorRotation = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)
        /*sensor view*/
        viewRoot = binding.relativeLayout
        circleMove = binding.circleMove
        motionSensor = MotionSensor(
            sensorManager,
            viewRoot,
            circleMove
        )
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
        motionSensor.registerListeners()
    }

    override fun onPause() {
        super.onPause()
        motionSensor.unregisterListener()
    }
}