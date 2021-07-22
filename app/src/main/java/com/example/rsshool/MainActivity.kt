package com.example.rsshool

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0

    private var job: Job? = null
    private val timerCoroutineViewModel by lazy {ViewModelProvider(this).get(TimerCoroutineViewModel::class.java)}

    private var isExitFromApp = false

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
            //timerAdapter.notifyItemInserted(timers.size - 1)
            timerAdapter.notifyDataSetChanged()
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        lifecycleScope.launch(Dispatchers.Main) {
//            while (true) {
//                delay(UNIT_TEN_MS)
//            }
//        }
    }

    private fun initialTimerTimeInput(): Long {
//        var timerTimeInputMinutesText = binding.timerMinutesInputText.text.toString()
//        var timerTimeInputSecondsText = binding.timerSecondsInputText.text.toString()
//        if (timerTimeInputMinutesText.isEmpty()) {
//            timerTimeInputMinutesText = "0"
//        }
//        if (timerTimeInputSecondsText.isEmpty()) {
//            timerTimeInputSecondsText = "0"
//        }
//        val timerTime = timerTimeInputMinutesText.toLong() * 60 * 1000 + timerTimeInputSecondsText.toLong() * 1000
//
        //binding.numberPickerTimerTimeSeconds.minValue = 0
        //binding.numberPickerTimerTimeSeconds.maxValue = 59

        val timerTimeMs =
            binding.numberPickerTimerTimeHours.value.toLong() * 1000 * 3600 +
            binding.numberPickerTimerTimeMinutes.value.toLong() * 1000 * 60 +
            binding.numberPickerTimerTimeSeconds.value.toLong() * 1000

        return timerTimeMs
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArray(LIST_OF_TIMERS, timers.toTypedArray())
        job?.let { timerCoroutineViewModel.saveTimerCoroutine(it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //Log.d("OnRestoreInstanceState", "onRestore")
        initTimersFromBundle(savedInstanceState)
    }

    private fun initTimersFromBundle(savedInstanceState: Bundle) {
        val arrayTimersFromBundle = savedInstanceState.getParcelableArray(LIST_OF_TIMERS) ?: return
        timerCoroutineViewModel.getTimerCoroutine().observe(this, { job ->
            this.job = job
        })
        timers.clear()
        for (timer in arrayTimersFromBundle) {
            timers.add(timer as Timer)
        }
        nextId = if (timers.isNotEmpty()) {
            timers[timers.size - 1].id + 1
        } else {
            0
        }
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.alert_dialog_text_confirmation))
            setMessage(getString(R.string.alert_dialog_text_question))


            setPositiveButton(getString(R.string.alert_dialog_text_yes)) { _, _ ->
                isExitFromApp = true
                super.onBackPressed()
            }

            setNegativeButton(getString(R.string.alert_dialog_text_no)){_, _ ->
//                Toast.makeText(this@MainActivity, "Thank you",
//                    Toast.LENGTH_LONG).show()
            }
            setCancelable(true)
        }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun start(id: Int) {
        timers.forEach { if (it.isStarted) it.isStarted = false}
        changeTimer(id, null, true)
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            val timerNullable = timers.find { it.id == id }
            if (timerNullable == null) cancel()
            val timer = timerNullable!!
            timer.currentMs += UNIT_ONE_SECOND
            while (true) {
                timer.currentMs -= UNIT_ONE_SECOND
                Log.d("MAIN timer - $id", timer.currentMs.toString())
                if (timer.currentMs <= 0L) {
                    timer.isStarted = false
                    //timer.currentMs = timer.initMs
                    timer.isAlarm = true
                    timerAdapter.submitList(timers.toList())
                    timerAdapter.notifyDataSetChanged()
                    cancel()
                }
                delay(UNIT_ONE_SECOND)
            }
        }
    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, false)
        timerAdapter.notifyDataSetChanged()

        job?.cancel()
    }

    override fun reset(id: Int, initMs: Long) {
        if (timers.find { it.id == id }?.isStarted == true) job?.cancel()

        changeTimer(id, initMs, false)
        timerAdapter.notifyDataSetChanged()
    }

    override fun delete(id: Int) {
        val timer = timers.find { it.id == id }
        if (timer?.isStarted == true) job?.cancel()

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
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (isExitFromApp) return
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