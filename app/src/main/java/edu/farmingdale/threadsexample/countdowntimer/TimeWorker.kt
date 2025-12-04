package edu.farmingdale.threadsexample.countdowntimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import edu.farmingdale.threadsexample.R
import kotlinx.coroutines.delay

const val CHANNEL_ID_TIMER = "channel_timer_with_sound"
const val NOTIFICATION_ID = 0
const val KEY_MILLIS_REMAINING = 2000

class TimerWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        var remainingMillis = inputData.getLong(KEY_MILLIS_REMAINING.toString(), 0)

        if (remainingMillis == 0L) {
            return Result.failure()
        }

        createTimerNotificationChannel()

        while (remainingMillis > 0) {
            postTimerNotification(timerText(remainingMillis), playSound = false)
            delay(1000)
            remainingMillis -= 1000
        }

        postTimerNotification("Timer is finished!", playSound = true)

        playBeep()

        return Result.success()
    }

    private fun createTimerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_TIMER,
                "Timer Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Displays how much time is left"
                enableVibration(false)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun postTimerNotification(text: String, playSound: Boolean) {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID_TIMER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (playSound) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }

        val notification = builder.build()

        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        Log.d("TimerWorker", text)
    }

    private fun playBeep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
    }
}