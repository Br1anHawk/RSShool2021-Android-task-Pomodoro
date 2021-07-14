package com.example.rsshool

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimerListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
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
            var timerTimeInputMinutesText = binding.timerMinutesInputText.text.toString()
            var timerTimeInputSecondsText = binding.timerSecondsInputText.text.toString()
            if (timerTimeInputMinutesText.isEmpty()) {
                timerTimeInputMinutesText = "0"
            }
            if (timerTimeInputSecondsText.isEmpty()) {
                timerTimeInputSecondsText = "0"
            }
            val timerTime = timerTimeInputMinutesText.toLong() * 60 * 1000 + timerTimeInputSecondsText.toLong() * 1000
            timers.add(Timer(nextId++, timerTime, false, timerTime))
            timerAdapter.submitList(timers.toList())
            timerAdapter.notifyItemInserted(timers.size - 1)
        }
    }

    override fun start(id: Int) {
        timers.forEach { if (it.isStarted) it.isStarted = false}
        //timerAdapter.notifyDataSetChanged()
        changeTimer(id, null, true)
        timerAdapter.submitList(timers.toList())
        timerAdapter.notifyDataSetChanged()
    }

    override fun stop(id: Int, currentMs: Long, adapterPosition: Int) {
        changeTimer(id, currentMs, false)
        timerAdapter.notifyItemChanged(adapterPosition)
    }

    override fun reset(id: Int, initMs: Long, adapterPosition: Int) {
        changeTimer(id, initMs, false)
        timerAdapter.notifyItemChanged(adapterPosition)
    }

    override fun delete(id: Int, adapterPosition: Int) {
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
            }
        //timerAdapter.submitList(timers)
        //timerAdapter.notifyDataSetChanged()
    }
}