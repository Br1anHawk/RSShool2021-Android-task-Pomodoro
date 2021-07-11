package com.example.rsshool

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool.databinding.StopwatchItemBinding

class StopWatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopWatchListener,
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopWatch: StopWatch) {
        binding.stopWatchTimer.text = stopWatch.currentMs.displayTime()
        if (stopWatch.isStarted) {
            startTimer(stopWatch)
        } else {
            stopTimer(stopWatch)
        }
        initButtonsListeners(stopWatch)
    }

    private fun initButtonsListeners(stopWatch: StopWatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopWatch.isStarted) {
                listener.stop(stopWatch.id, stopWatch.currentMs)
            } else {
                listener.start(stopWatch.id)
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopWatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopWatch.id) }
    }

    private fun startTimer(stopWatch: StopWatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()
        timer = getCountDownTimer(stopWatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopWatch: StopWatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopWatch: StopWatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                stopWatch.currentMs += interval
                binding.stopWatchTimer.text = stopWatch.currentMs.displayTime()
            }

            override fun onFinish() {
                binding.stopWatchTimer.text = stopWatch.currentMs.displayTime()
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
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD  = 1000L * 60L * 60L * 24L // Day
    }
}