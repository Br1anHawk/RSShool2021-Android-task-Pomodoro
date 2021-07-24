package com.example.rsshool.timer.logic

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool.*
import com.example.rsshool.databinding.TimerItemBinding
import kotlinx.coroutines.*


class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timerClock: CountDownTimer? = null
    private var job: Job? = null

    private val colorCardviewBackground =
        binding
            .root
            .context
            .theme
            .obtainStyledAttributes(null, R.styleable.TimerNotificationAlarmed, 0, 0)
            .getColorStateList(R.styleable.TimerNotificationAlarmed_colorOnPrimary)
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
            binding.startStopTimerButton.isEnabled = false
        } else {
            binding.startStopTimerButton.isEnabled = true
            binding.timerCardview.setCardBackgroundColor(colorCardviewBackground)
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
                job?.cancel()
                listener.stop(timer.id, timer.currentMs, adapterPosition)
            } else {
                listener.start(timer.id)
            }
        }

        binding.restartButton.setOnClickListener {
            if (timer.isStarted) {
                this.timerClock?.cancel()
                job?.cancel()
            }
            listener.reset(timer.id, timer.initMs, adapterPosition)
        }

        binding.deleteButton.setOnClickListener {
            if (timer.isStarted) {
                this.timerClock?.cancel()
                job?.cancel()
            }
            listener.delete(timer.id)
        }
    }

    private fun startTimer(timer: Timer) {
        //binding.timerCardview.setCardBackgroundColor(ColorStateList.valueOf(resources.getColor(R.color.white)))
        binding.startStopTimerButton.text = resources.getString(R.string.button_timer_stop_text)

        this.timerClock?.cancel()
        //this.timerClock = getCountDownTimer(timer)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (timer.currentMs > 0L) {
                binding.timerDisplayTextview.text = timer.currentMs.displayTime()
                binding.circleProgressBarView.setCurrent(timer.currentMs)
                delay(UNIT_TEN_MS)
            }
            binding.timerCardview.setCardBackgroundColor(colorNotificationTimerAlarmed)
            binding.startStopTimerButton.isEnabled = false
            stopTimer(timer)
        }
        this.timerClock?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(timer: Timer) {
        binding.startStopTimerButton.text = resources.getString(R.string.button_timer_start_text)
        this.timerClock?.cancel()
        job?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(timer.initMs, UNIT_ONE_HUNDRED_MS) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d("CDT_currentMs", "${timer.currentMs}")
                Log.d("CDT_untilFinish", millisUntilFinished.toString())
                binding.timerDisplayTextview.text = timer.currentMs.displayTime()
                binding.circleProgressBarView.setCurrent(timer.currentMs)
            }

            override fun onFinish() {
                //Log.d("onFinish_CountDownTimer", "${timer.currentMs}")
                //binding.timerDisplayTextview.text = timer.initMs.displayTime()
                //binding.circleProgressBarView.setCurrent(timer.initMs)
                binding.startStopTimerButton.isEnabled = false
                stopTimer(timer)
                //binding.timerCardview.setCardBackgroundColor(colorNotificationTimerAlarmed)
            }
        }
    }
}