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
import com.upriseus.remindme.features.reminders.ReminderListener
import com.upriseus.remindme.features.reminders.Reminders
import com.upriseus.remindme.features.reminders.RemindersActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

 class JobServiceNotif : JobService(), ReminderListener {

    companion object{
        private const val CHANNEL_ID = "RemindME_Notification_Channel"
        var NOTIFICATION_ID : Int = 0
        var MESSAGE : String = ""
        var RECURRING : Boolean = false
        var DAYOFWEEK : Int = 10
        var GEOFENCE : Boolean = false
        lateinit var REMINDER : Reminders
        var KEY : String = ""
        var CREATOR : String = ""
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val job = Job()

        if (params != null) {
            NOTIFICATION_ID = params.extras.get("notificationID") as Int
            KEY = params.extras.get("key") as String
            CREATOR = params.extras.getString("creator").toString()
        }

        CoroutineScope(Dispatchers.Default + job).launch {
            val reminderActions = RemindersActions(this@JobServiceNotif)
            reminderActions.getOwnedReminders(CREATOR)
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

     override fun onReminderReceived(reminder: MutableList<Reminders>) {
         for (remind in reminder){
             if(remind.uuid == KEY){
                 MESSAGE = remind.message
                 RECURRING = remind.recurring == true
                 if(RECURRING){
                    DAYOFWEEK = remind.dayOfWeek!!
                 }
                 GEOFENCE = remind.locationx != null
                 REMINDER = remind
                notification()
             }
         }
     }

     private fun notification(){
         val context = this
         val notifyIntent = Intent(context, MainActivity::class.java)
         val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0)

         val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
             putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
             putExtra("reminder", HashMap<String, Any?>(REMINDER.toMap()))
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
             if(GEOFENCE){
                 if(REMINDER.inGeofence){
                     notify(NOTIFICATION_ID, notificationBuilder.build())
                 }else{
                     JobSchedulerNotif.snoozeJob(context, REMINDER, 5 * 1000 * 60, NOTIFICATION_ID) //retry in five minutes
                 }
             }else{
                 notify(NOTIFICATION_ID, notificationBuilder.build())
             }
         }
         if(RECURRING){
             JobSchedulerNotif.weeklyJob(context, REMINDER, DAYOFWEEK, NOTIFICATION_ID) //as notificationId is the same as the jobId
         }
     }

     override fun onError(error: Throwable?) {
         TODO("Not yet implemented")
     }

     override fun onReminderDeleted(reminder: Reminders) {
         TODO("Not yet implemented")
     }
 }