package com.example.rsshool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), StopWatchListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val stopWatchAdapter = StopWatchAdapter(this)
    private val stopWatches = mutableListOf<StopWatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopWatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            stopWatches.add(StopWatch(nextId++, 0, true))
            stopWatchAdapter.submitList(stopWatches.toList())
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun reset(id: Int) {
        changeStopwatch(id, 0L, false)
    }

    override fun delete(id: Int) {
        stopWatches.remove(stopWatches.find { it.id == id })
        stopWatchAdapter.submitList(stopWatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<StopWatch>()
        stopWatches.forEach {
            if (it.id == id) {
                newTimers.add(StopWatch(it.id, currentMs ?: it.currentMs, isStarted))
            } else {
                newTimers.add(it)
            }
        }
        stopWatchAdapter.submitList(newTimers)
        stopWatches.clear()
        stopWatches.addAll(newTimers)
    }
}