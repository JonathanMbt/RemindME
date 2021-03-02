 package com.upriseus.remindme.features.notifications

import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.upriseus.remindme.MainActivity
import com.upriseus.remindme.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

 class JobServiceNotif : JobService() {

    companion object{
        private const val CHANNEL_ID = "RemindME_Notification_Channel"
        var NOTIFICATION_ID : Int = 0
        var MESSAGE : String = ""
        var RECURRING : Boolean = false
        var DAYOFWEEK : Int = 10
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val job = Job()
        val context = this

        if (params != null) {
            MESSAGE = params.extras.get("reminderMessage") as String
            NOTIFICATION_ID = params.extras.get("notificationID") as Int
            RECURRING = params.extras.get("recurring") as Boolean
            DAYOFWEEK = params.extras.get("dayOfWeek") as Int
        }

        CoroutineScope(Dispatchers.Default + job).launch {
            val notifyIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0)

            val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
                putExtra("message", MESSAGE)
            }

            // INFO : https://stackoverflow.com/a/24582168
            val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, Random().nextInt(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_hourglass)
                setContentTitle(getString(R.string.app_name))
                setContentText(MESSAGE)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setGroup(CHANNEL_ID)
                setContentIntent(pendingIntent)
                setAutoCancel(true) //delete notification when clicked (app launched)
                addAction(R.drawable.snooze, getString(R.string.snooze), snoozePendingIntent)
            }
            createNotificationChannel()
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        }
        if(RECURRING){
            JobSchedulerNotif.weeklyJob(context, MESSAGE, DAYOFWEEK, NOTIFICATION_ID) //as notificationId is the same as the jobId
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobFinished(params, false)
        return false
    }

    private fun isChannel() : Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    private fun createNotificationChannel(){
        if(isChannel()){
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                description = getString(R.string.app_name)
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}