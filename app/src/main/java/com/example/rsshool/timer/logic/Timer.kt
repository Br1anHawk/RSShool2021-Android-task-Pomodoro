package com.example.rsshool.timer.logic

import android.os.Parcel
import android.os.Parcelable

data class Timer(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    val initMs: Long,
    var isAlarm: Boolean
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(currentMs)
        parcel.writeByte(if (isStarted) 1 else 0)
        parcel.writeLong(initMs)
        parcel.writeByte(if (isStarted) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Timer> {
        override fun createFromParcel(parcel: Parcel): Timer {
            return Timer(parcel)
        }

        override fun newArray(size: Int): Array<Timer?> {
            return arrayOfNulls(size)
        }
    }
}