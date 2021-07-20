package com.example.rsshool

const val START_TIME = "00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val LIST_OF_TIMERS = "LIST_OF_TIMERS"
const val TIMER_CURRENT_MS_TIME = "TIMER_CURRENT_MS_TIME"
const val UNIT_ONE_SECOND = 1000L
const val UNIT_TEN_MS = 10L
const val RUNNING_TIMER_ID = "RUNNING_TIMER_ID"
const val CHANNEL_ID = "Channel_ID"
const val NOTIFICATION_ID = 777
const val TIMER_COROUTINE = "TIMER_COROUTINE"

fun Long.displayTime(): String {
    if (this <= 0L) {
        return START_TIME
    }
    val h = this / 1000 / 3600
    val m = this / 1000 % 3600 / 60
    val s = this / 1000 % 60
    //val ms = this % 1000 / 10

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}