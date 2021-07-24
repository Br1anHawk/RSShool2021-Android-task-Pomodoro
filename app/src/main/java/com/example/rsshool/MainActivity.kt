package com.example.rsshool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool.databinding.ActivityMainBinding
import com.example.rsshool.timer.logic.Timer
import com.example.rsshool.timer.logic.TimerAdapter
import com.example.rsshool.timer.logic.TimerCoroutineViewModel
import com.example.rsshool.timer.logic.TimerListener
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0

    private var job: Job? = null
    private val timerCoroutineViewModel by lazy {ViewModelProvider(this).get(TimerCoroutineViewModel::class.java)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            val timerTimeMs = initialTimerTimeInput()
            if (timerTimeMs == 0L) {
                return@setOnClickListener
            }
            timers.add(Timer(nextId++, timerTimeMs, false, timerTimeMs, false))
            timerAdapter.submitList(timers.toList())
            timerAdapter.notifyDataSetChanged()
            //timerAdapter.notifyItemInserted(timers.size - 1)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    }

    private fun initialTimerTimeInput(): Long {
        return binding.numberPickerTimerTimeHours.value.toLong() * 1000 * 3600 +
                binding.numberPickerTimerTimeMinutes.value.toLong() * 1000 * 60 +
                binding.numberPickerTimerTimeSeconds.value.toLong() * 1000
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //timerCoroutineViewModel.saveTimersList(timers.toList())
        outState.putParcelableArray(LIST_OF_TIMERS, timers.toTypedArray())
        job?.let { timerCoroutineViewModel.saveTimerCoroutine(it) }
        timerCoroutineViewModel.saveTimerInput(
            arrayOf(
                binding.numberPickerTimerTimeHours.value,
                binding.numberPickerTimerTimeMinutes.value,
                binding.numberPickerTimerTimeSeconds.value
            )
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        initTimersFromBundle(savedInstanceState)
    }

    private fun initTimersFromBundle(savedInstanceState: Bundle) {
        timers.clear()
        timerCoroutineViewModel.getTimersList().observe(this, { timers ->
            timers.forEach { this.timers.add(it) }
        })
        savedInstanceState.getParcelableArray(LIST_OF_TIMERS)?.forEach { timers.add(it as Timer) }
        nextId = if (timers.isNotEmpty()) {
            timers[timers.size - 1].id + 1
        } else {
            0
        }
        timerCoroutineViewModel.getTimerCoroutine().observe(this, { job ->
            this.job = job
        })
        timerCoroutineViewModel.getTimerInput().observe(this, {timerInput ->
            with(binding) {
                numberPickerTimerTimeHours.value = timerInput[0]
                numberPickerTimerTimeMinutes.value = timerInput[1]
                numberPickerTimerTimeSeconds.value = timerInput[2]
            }
        })
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.alert_dialog_text_confirmation))
            setMessage(getString(R.string.alert_dialog_text_question))

            setPositiveButton(getString(R.string.alert_dialog_text_yes)) { _, _ ->
                timers.forEach { if (it.isStarted) it.isStarted = false}
                job?.cancel()
                finishAndRemoveTask()
            }

            setNegativeButton(getString(R.string.alert_dialog_text_no)){_, _ ->

            }
            setCancelable(true)
        }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        onAppForegrounded()
        _binding = null
    }

    override fun start(id: Int) {
        timers.forEach { if (it.isStarted) it.isStarted = false}
        changeTimer(id, null, true)
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            var currentTime = SystemClock.elapsedRealtime()
            val timer = timers.find { it.id == id }
            if (timer == null) {
                cancel()
                return@launch
            }
            //timer.currentMs += UNIT_ONE_SECOND
            while (true) {
                timer.currentMs -= SystemClock.elapsedRealtime() - currentTime
                currentTime = SystemClock.elapsedRealtime()
                //timer.currentMs -= UNIT_TEN_MS
                //Log.d("MAIN timer - $id", timer.currentMs.toString())
                if (timer.currentMs <= 0L) {
                    timer.isStarted = false
                    timer.isAlarm = true
                    timerAdapter.submitList(timers.toList())
                    timerAdapter.notifyDataSetChanged()
                    cancel()
                }
                delay(UNIT_TEN_MS)
            }
        }
    }

    override fun stop(id: Int, currentMs: Long, adapterItemId: Int) {
        changeTimer(id, currentMs, false)
        //timerAdapter.notifyDataSetChanged()
        timerAdapter.notifyItemChanged(adapterItemId)
        job?.cancel()
    }

    override fun reset(id: Int, initMs: Long, adapterItemId: Int) {
        if (timers.find { it.id == id }?.isStarted == true) job?.cancel()
        changeTimer(id, initMs, false)
        //timerAdapter.notifyDataSetChanged()
        timerAdapter.notifyItemChanged(adapterItemId)
    }

    override fun delete(id: Int) {
        val timer = timers.find { it.id == id }
        if (timer?.isStarted == true) {
            timer.isStarted = false
            job?.cancel()
        }
        timers.remove(timer)
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()
    }

    private fun changeTimer(id: Int, currentMs: Long?, isStarted: Boolean) {
        timers
            .find { it.id == id }
            ?.let {
                it.currentMs = currentMs ?: it.currentMs
                it.isStarted = isStarted
                it.isAlarm = false
            }
        //timerAdapter.submitList(timers.toList())
        //timerAdapter.notifyDataSetChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val runningTimer = timers.find { it.isStarted } ?: return
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(TIMER_CURRENT_MS_TIME, runningTimer.currentMs)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}