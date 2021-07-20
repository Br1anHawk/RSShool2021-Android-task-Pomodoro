package com.example.rsshool

import android.content.res.ColorStateList
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

    private var timerClock: CountDownTimer? = null
    private val colorNotificationTimerAlarmed =
        binding
            .root
            .context
            .theme
            .obtainStyledAttributes(null, R.styleable.TimerNotificationAlarmed, 0, 0)
            .getColorStateList(R.styleable.TimerNotificationAlarmed_colorPrimarySurface)

    fun bind(timer: Timer) {
        if (timer.isAlarm) {
            binding.timerCardview.setCardBackgroundColor(colorNotificationTimerAlarmed)
        } else {
            binding.timerCardview.setCardBackgroundColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
        }
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
                this.timerClock?.cancel()
                listener.stop(timer.id, timer.currentMs)
            } else {
                listener.start(timer.id)
            }
        }

        binding.restartButton.setOnClickListener {
            if (timer.isStarted) {
                this.timerClock?.cancel()
            }
            listener.reset(timer.id, timer.initMs)
        }

        binding.deleteButton.setOnClickListener {
            if (timer.isStarted) {
                this.timerClock?.cancel()
            }
            listener.delete(timer.id)
        }
    }

    private fun startTimer(timer: Timer) {
        //binding.timerCardview.setCardBackgroundColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
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

            override fun onTick(millisUntilFinished: Long) {
                //Log.d("onTick_CountDownTimer", "${timer.currentMs}")
                binding.timerDisplayTextview.text = timer.currentMs.displayTime()
                binding.circleProgressBarView.setCurrent(timer.currentMs)
            }

            override fun onFinish() {
                //Log.d("onFinish_CountDownTimer", "${timer.currentMs}")
                binding.timerDisplayTextview.text = timer.initMs.displayTime()
                binding.circleProgressBarView.setCurrent(timer.initMs)
                stopTimer(timer)
                //binding.timerCardview.setCardBackgroundColor(colorNotificationTimerAlarmed)
            }
        }
    }
}