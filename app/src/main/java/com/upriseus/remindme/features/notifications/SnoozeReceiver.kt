package com.upriseus.remindme.features.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.upriseus.remindme.features.reminders.Reminders


class SnoozeReceiver : BroadcastReceiver(){
    lateinit var reminders: Reminders
    override fun onReceive(context: Context?, intent: Intent?) {
        var id = 0


        if(intent?.extras != null){
            id = intent.extras?.get(Notification.EXTRA_NOTIFICATION_ID) as Int
            reminders = Reminders.toObject(intent.extras?.getSerializable("reminder") as Map<String, Any?>)
        }

        val notificationManager : NotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
        JobSchedulerNotif.CREATOR = reminders.creatorId.toString()
        JobSchedulerNotif.snoozeJob(context, reminders, 1000 * 60 * 5, id) //5min
    }

}
