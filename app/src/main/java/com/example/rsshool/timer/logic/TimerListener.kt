package com.example.rsshool.timer.logic

interface TimerListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun reset(id: Int, initMs: Long)

    fun delete(id: Int)
}