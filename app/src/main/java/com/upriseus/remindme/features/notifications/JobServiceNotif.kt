package com.upriseus.remindme.features.notifications

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
import com.upriseus.remindme.SignActivity
import com.upriseus.remindme.features.reminders.Reminders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class JobServiceNotif : JobService() {

    companion object{
        private const val CHANNEL_ID = "RemindME_Notification_Channel"
        var NOTIFICATION_ID : Int = 0
        lateinit var reminder : Reminders
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val job = Job()
        val context = this
        CoroutineScope(Dispatchers.Default + job).launch {
            // TODO : add a receiver to make the reminders seen (because tap)
            val notifyIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_hourglass)
                setContentTitle(getString(R.string.app_name))
                setContentText(reminder.message)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setGroup(CHANNEL_ID)
                setContentIntent(pendingIntent)
                setAutoCancel(true) //delete notification when clicked (app launched)
            }
            createNotificationChannel()
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notificationBuilder.build())
            }
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