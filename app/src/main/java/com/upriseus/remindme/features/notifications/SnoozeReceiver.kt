package com.upriseus.remindme.features.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat


class SnoozeReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        var id = 0
        var message = ""

        if(intent?.extras != null){
            id = intent.extras?.get(Notification.EXTRA_NOTIFICATION_ID) as Int
            message = intent.extras?.getString("message").toString()
        }

        val notificationManager : NotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)

        JobSchedulerNotif.snoozeJob(context, message, 1000 * 60 * 1) //5min
    }

}
