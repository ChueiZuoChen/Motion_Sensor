package com.example.motionsensor

import android.animation.ObjectAnimator
import android.os.CountDownTimer
import android.view.View
import com.example.motionsensor.databinding.ActivityMainBinding

class CountDown(
    private val binding: ActivityMainBinding,
    private val countDownListener: CountDownListener,
) {
    var progressAnimator: ObjectAnimator? = null
    var actionCounter: CountDownTimer? = null

    init {
        val progressBar = binding.circleCountdownProgressbar
        progressAnimator = ObjectAnimator.ofInt(
            binding.circleCountdownProgressbar,
            "progress",
            progressBar.progress,
            progressBar.progress + 100
        )
        progressAnimator?.duration = 5000
    }

    companion object {
        const val millisInFuture = 5000L
        const val countDownInterval = 1000L
    }

    fun startCountDown() {
        actionCounter = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val countDownSecond = millisUntilFinished / countDownInterval
                //TODO: action for each second
                countDownListener.countDownSeconds(countDownSecond)
            }

            override fun onFinish() {
                //TODO completed action
                countDownListener.isCountDownCompleted(true)
            }
        }
        progressAnimator?.start()
        actionCounter?.start()
    }

    fun stopCountDown() {
        progressAnimator?.cancel()
        actionCounter?.cancel()
        binding.text.visibility = View.INVISIBLE
    }
}

interface CountDownListener {
    fun countDownSeconds(second: Long)
    fun isCountDownCompleted(completed: Boolean = false)
}