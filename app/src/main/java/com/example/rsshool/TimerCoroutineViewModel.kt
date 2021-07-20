package com.example.rsshool

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class TimerCoroutineViewModel: ViewModel() {

    private val job: MutableLiveData<Job?> = MutableLiveData()

    fun saveTimerCoroutine(job: Job?) {
        this.job.value = job
    }

    fun getTimerCoroutine(): LiveData<Job?> {
        return job
    }
}