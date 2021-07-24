package com.example.rsshool.timer.logic

interface TimerListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long, adapterItemId: Int)

    fun reset(id: Int, initMs: Long, adapterItemId: Int)

    fun delete(id: Int)
}