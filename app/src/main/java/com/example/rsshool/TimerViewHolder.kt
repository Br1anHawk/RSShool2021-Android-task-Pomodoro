package com.example.rsshool

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool.databinding.TimerItemBinding

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(timer: Timer) {
        binding.timerDisplayTextview.text = timer.currentMs.displayTime()
        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }
        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startStopTimerButton.setOnClickListener {
            if (timer.isStarted) {
                listener.stop(timer.id, timer.currentMs)
            } else {
                listener.start(timer.id)
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(timer.id, timer.initMs) }

        binding.deleteButton.setOnClickListener { listener.delete(timer.id) }
    }

    private fun startTimer(timer: Timer) {
        binding.startStopTimerButton.text = resources.getString(R.string.button_timer_stop_text)

        this.timer?.cancel()
        this.timer = getCountDownTimer(timer)
        this.timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(timer: Timer) {
        binding.startStopTimerButton.text = resources.getString(R.string.button_timer_start_text)

        this.timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                timer.currentMs -= interval
                binding.timerDisplayTextview.text = timer.currentMs.displayTime()
            }

            override fun onFinish() {
                binding.timerDisplayTextview.text = timer.currentMs.displayTime()
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        //val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 1000L
        private const val PERIOD  = 1000L * 60L * 60L * 24L // Day
    }
}