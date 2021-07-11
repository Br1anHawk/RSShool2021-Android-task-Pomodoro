package com.example.rsshool

interface TimerListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long, adapterPosition: Int)

    fun reset(id: Int, initMs: Long, adapterPosition: Int)

    fun delete(id: Int, adapterPosition: Int)
}