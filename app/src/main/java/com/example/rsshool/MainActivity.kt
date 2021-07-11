package com.example.rsshool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimerListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val timerAdapter = TimerAdapter(this)
    private val timer = mutableListOf<Timer>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            val timerTimeText = binding.timerMinutesInputText.text.toString()
            if (timerTimeText.isNotEmpty()) {
                val timerTime = timerTimeText.toLong() * 60 * 1000
                timer.add(Timer(nextId++, timerTime, false, timerTime))
                timerAdapter.submitList(timer.toList())
            }
        }
    }

    override fun start(id: Int) {
        changeTimer(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeTimer(id, currentMs, false)
    }

    override fun reset(id: Int, initMs: Long) {
        changeTimer(id, initMs, false)
    }

    override fun delete(id: Int) {
        timer.remove(timer.find { it.id == id })
        timerAdapter.submitList(timer.toList())
    }

    private fun changeTimer(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()
        timer.forEach {
            if (it.id == id) {
                newTimers.add(Timer(it.id, currentMs ?: it.currentMs, isStarted, it.initMs))
            } else {
                newTimers.add(it)
            }
        }
        timerAdapter.submitList(newTimers)
        timer.clear()
        timer.addAll(newTimers)
    }
}