package com.example.rsshool

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool.databinding.TimerItemBinding

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timerClock: CountDownTimer? = null

    fun bind(timer: Timer) {
        binding.timerDisplayTextview.text = timer.currentMs.displayTime()
        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }
        initButtonsListeners(timer)

        binding.circleProgressBarView.apply {
            //layoutParams = ViewGroup.LayoutParams(height, height)
            setPeriod(timer.initMs)
            setCurrent(timer.currentMs)
        }
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startStopTimerButton.setOnClickListener {
            if (timer.isStarted) {
                listener.stop(timer.id, timer.currentMs, adapterPosition)
            } else {
                listener.start(timer.id)
            }
        }

        binding.restartButton.setOnClickListener {
            if (timer.isStarted) {
                this.timerClock?.cancel()
            }
            listener.reset(timer.id, timer.initMs, adapterPosition)
        }

        binding.deleteButton.setOnClickListener {
            if (timer.isStarted) {
                this.timerClock?.cancel()
            }
            listener.delete(timer.id, adapterPosition)
        }
    }

    private fun startTimer(timer: Timer) {
        binding.timerCardview.setCardBackgroundColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
        binding.startStopTimerButton.text = resources.getString(R.string.button_timer_stop_text)

        this.timerClock?.cancel()
        this.timerClock = getCountDownTimer(timer)
        this.timerClock?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(timer: Timer) {
        binding.startStopTimerButton.text = resources.getString(R.string.button_timer_start_text)

        this.timerClock?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(timer.currentMs, UNIT_ONE_SECOND) {
            val interval = UNIT_ONE_SECOND

            override fun onTick(millisUntilFinished: Long) {
                Log.d("onTick_CountDownTimer", "${timer.currentMs}")
                timer.currentMs -= interval
                binding.timerDisplayTextview.text = timer.currentMs.displayTime()
                binding.circleProgressBarView.setCurrent(timer.currentMs)
            }

            override fun onFinish() {
                Log.d("onFinish_CountDownTimer", "${timer.currentMs}")
                binding.timerDisplayTextview.text = timer.initMs.displayTime()
                timer.isStarted = false
                timer.currentMs = timer.initMs
                stopTimer(timer)
                binding.timerCardview.setCardBackgroundColor(ColorStateList.valueOf(resources.getColor(R.color.stop_timer_notification_color)))
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
        private const val UNIT_TEN_MS = 10L
        private const val UNIT_ONE_SECOND = 1000L
    }
}