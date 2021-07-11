package com.example.rsshool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val stopWatchAdapter = StopWatchAdapter()
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
            stopWatches.add(StopWatch(nextId++, 0, false))
            stopWatchAdapter.submitList(stopWatches.toList())
        }
    }

}