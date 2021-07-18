package com.example.rsshool

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    override fun onStart() {
        super.onStart()
        Log.d("OnStart", "onStart")
        //if(timers.isNotEmpty()) return
        val arrayOfTimers = this.intent.extras?.getParcelableArray(LIST_OF_TIMERS)
        timers.clear()
        arrayOfTimers?.forEach { timers.add(it as Timer) }
        timerAdapter.submitList(timers)
        //timerAdapter.notifyItemRangeInserted(0, timers.size - 1)
        timerAdapter.notifyDataSetChanged()
        nextId = if (timers.isNotEmpty()) {
            timers[timers.size - 1].id + 1
        } else {
            0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            var timerTimeInputMinutesText = binding.timerMinutesInputText.text.toString()
            var timerTimeInputSecondsText = binding.timerSecondsInputText.text.toString()
            if (timerTimeInputMinutesText.isEmpty()) {
                timerTimeInputMinutesText = "0"
            }
            if (timerTimeInputSecondsText.isEmpty()) {
                timerTimeInputSecondsText = "0"
            }
            val timerTime = timerTimeInputMinutesText.toLong() * 60 * 1000 + timerTimeInputSecondsText.toLong() * 1000
            if (timerTime == 0L) {
                return@setOnClickListener
            }
            timers.add(Timer(nextId++, timerTime, false, timerTime, false))
            timerAdapter.submitList(timers.toList())
            timerAdapter.notifyItemInserted(timers.size - 1)
            //timerAdapter.notifyDataSetChanged()
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

//        lifecycleScope.launch(Dispatchers.Main) {
//            while (true) {
//
//                delay(10L)
//            }
//        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArray(LIST_OF_TIMERS, timers.toTypedArray())
        val runningTimer = timers.find { it.isStarted } ?: return
        outState.putInt(RUNNING_TIMER_ID, runningTimer.id)
        //timers.forEach { if (it.isStarted) it.isStarted = false}
        //runningTimer.isStarted = false
        timerAdapter.submitList(timers.toList())
        var adapterPosition = 0
        for (i in timers.indices) {
            if (timers[i].id == runningTimer.id) {
                adapterPosition = i
                break
            }
        }
        timerAdapter.notifyItemChanged(adapterPosition)
        //timerAdapter.notifyDataSetChanged()
        //runningTimer.isStarted = true
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("OnRestoreInstanceState", "onRestore")
        initTimersFromBundle(savedInstanceState)
    }

    private fun initTimersFromBundle(savedInstanceState: Bundle) {
        val arrayTimersFromBundle = savedInstanceState.getParcelableArray(LIST_OF_TIMERS) ?: return
        val runningTimerId = savedInstanceState.getInt(RUNNING_TIMER_ID)
        timers.clear()
        for (timer in arrayTimersFromBundle) {
            if ((timer as Timer).id == runningTimerId) timer.isStarted = true
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

    override fun onDestroy() {
        super.onDestroy()
        this.intent.extras?.putParcelableArray(LIST_OF_TIMERS, timers.toTypedArray())
        _binding = null
    }

    override fun start(id: Int, adapterPosition: Int) {
        timers.forEach { if (it.isStarted) it.isStarted = false}
        changeTimer(id, null, true)
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()

        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            val timerNullable = timers.find { it.id == id }
            if (timerNullable == null) cancel()
            val timer = timerNullable!!
            while (true) {
                timer.currentMs -= UNIT_ONE_SECOND
                Log.d("MAIN timer - $id", timer.currentMs.toString())
                if (timer.currentMs <= 0L) {
                    timer.isStarted = false
                    timer.currentMs = timer.initMs
                    timer.isAlarm = true
                    cancel()
                }
                delay(UNIT_ONE_SECOND)
            }
        }
    }

    override fun stop(id: Int, currentMs: Long, adapterPosition: Int) {
        changeTimer(id, currentMs, false)
        timerAdapter.notifyItemChanged(adapterPosition)

        job?.cancel()
    }

    override fun reset(id: Int, initMs: Long, adapterPosition: Int) {
        if (timers.find { it.id == id }?.isStarted == true) job?.cancel()

        changeTimer(id, initMs, false)
        timerAdapter.notifyItemChanged(adapterPosition)
    }

    override fun delete(id: Int, adapterPosition: Int) {
        if (timers.find { it.id == id }?.isStarted == true) job?.cancel()

        changeTimer(id, null, false)
        timerAdapter.notifyItemChanged(adapterPosition)
        timers.remove(timers.find { it.id == id })
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
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(LIST_OF_TIMERS, timers.toTypedArray())
        //startIntent.putExtra(STARTED_TIMER_TIME_MS, System.currentTimeMillis())
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}