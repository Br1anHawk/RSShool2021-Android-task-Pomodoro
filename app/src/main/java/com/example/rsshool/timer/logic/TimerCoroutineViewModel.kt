package com.example.rsshool.timer.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class TimerCoroutineViewModel: ViewModel() {

    private val timers: MutableLiveData<List<Timer>> = MutableLiveData()
    private val job: MutableLiveData<Job?> = MutableLiveData()
    private val timerInput: MutableLiveData<Array<Int>> = MutableLiveData()

    fun saveTimersList(timers: List<Timer>?) {
        this.timers.value = timers
    }

    fun getTimersList(): LiveData<List<Timer>> {
        return timers
    }

    fun saveTimerCoroutine(job: Job?) {
        this.job.value = job
    }

    fun getTimerCoroutine(): LiveData<Job?> {
        return job
    }

    fun saveTimerInput(timerInput: Array<Int>) {
        this.timerInput.value = timerInput
    }

    fun getTimerInput(): LiveData<Array<Int>> {
        return timerInput
    }
}