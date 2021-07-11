package com.example.rsshool

data class StopWatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)