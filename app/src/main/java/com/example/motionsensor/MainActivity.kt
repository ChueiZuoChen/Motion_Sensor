package com.example.motionsensor

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModelProvider
import com.example.motionsensor.databinding.ActivityMainBinding

enum class State {
    AlignmentStart,
    CountDown,
    AlignmentCompleted
}

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OrientationListener, CountDownListener {
    lateinit var binding: ActivityMainBinding
    lateinit var preview: Preview
    var selector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    var cameraProvider: ProcessCameraProvider? = null
    lateinit var viewModel: AlignmentViewModel
    lateinit var countDown: CountDown
    /*Sensor*/
    lateinit var motionSensor: MotionSensor
    /*SensorView*/
    lateinit var circleMove: ImageView
    lateinit var circleFix: ImageView
    lateinit var circleCountdownProgressbar:ProgressBar
    lateinit var viewRoot: RelativeLayout
    lateinit var countDownLabel:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preview = Preview.Builder()
            .setTargetResolution(Size(720, 1280))
            .build()
            .also {
                it.setSurfaceProvider(binding.preview.surfaceProvider)
            }
        cameraProvider = ProcessCameraProvider.getInstance(this).get()
        cameraProvider!!.bindToLifecycle(this, selector, preview)


        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_NOSENSOR)

        /*sensor view*/
        viewRoot = binding.relativeLayout
        circleMove = binding.circleMove
        circleFix = binding.circleFix
        countDownLabel = binding.text
        circleCountdownProgressbar = binding.circleCountdownProgressbar
        circleFix.visibility = View.VISIBLE
        circleMove.visibility = View.VISIBLE
        motionSensor = MotionSensor(
            viewRoot,
            circleMove,
            this,
            this
        )
        countDown = CountDown(binding, this)
        viewModel = ViewModelProvider(this).get(AlignmentViewModel::class.java)
        viewModel.state.observe(this) {
            when (it) {
                State.AlignmentStart -> {
                    circleFix.visibility = View.VISIBLE
                    circleMove.visibility = View.VISIBLE
                    circleCountdownProgressbar.visibility = View.INVISIBLE
                }
                State.CountDown -> {
                    circleFix.visibility = View.INVISIBLE
                    circleMove.visibility = View.INVISIBLE
                    countDownLabel.visibility = View.VISIBLE
                    circleCountdownProgressbar.visibility = View.VISIBLE
                    countDown.startCountDown()
                }
                State.AlignmentCompleted -> {
                    // scale contour + camera capture
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        motionSensor.registerMotionSensorListeners()
    }

    override fun onPause() {
        super.onPause()
        motionSensor.unregisterMotionSensorListener()
    }

    override fun isWrongOrientation(isInZRotateRange: Boolean, isInXRotateRange: Boolean):Boolean {
        if (isInZRotateRange && isInXRotateRange) {
            viewModel.setState(State.CountDown)
            return true
        } else {
            countDownLabel.textSize = 70f
            countDown.stopCountDown()
            viewModel.setState(State.AlignmentStart)
            return false
        }
    }

    override fun getCountDownSeconds(second: Long) {
        Log.d(TAG, "countDownSeconds: $second")
        countDownLabel.text = second.toString()
    }

    override fun isCountDownCompleted(completed: Boolean) {
        if (completed) {
            countDownLabel.visibility = View.VISIBLE
            countDownLabel.textSize = 20f
            countDownLabel.text = "Finished"
            viewModel.setState(State.AlignmentCompleted)
        }
    }
}