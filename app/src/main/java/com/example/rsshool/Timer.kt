package com.example.rsshool

data class Timer(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    val initMs: Long
)