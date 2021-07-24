package com.example.rsshool

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    private var ringtone: Ringtone? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_content_title_name))
            .setGroup(getString(R.string.service_notification_group_name))
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.blinking_tomato_indicator)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_app_foreground))
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        ringtone =
            RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val time = intent?.extras?.getLong(TIMER_CURRENT_MS_TIME)
                    ?: return
                commandStart(time)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart(time: Long) {
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(time)
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer(timerTime: Long) {
        var time = timerTime
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                if (time <= 0L) {
                    ringtone?.play()
                    break
                } else {
                    time -= UNIT_ONE_SECOND
                }
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        time.displayTime()
                    )
                )
                delay(UNIT_ONE_SECOND)
            }
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        //notificationManager?.notify(NOTIFICATION_ID, builder.setContentIntent(getPendingIntent()).build())

        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val intentToMainActivity = Intent(this, MainActivity::class.java)
        intentToMainActivity.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, intentToMainActivity, PendingIntent.FLAG_ONE_SHOT)
    }
}