package com.upriseus.remindme.features.notifications

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log
import com.upriseus.remindme.features.reminders.Reminders
import java.util.*
import kotlin.random.Random

object JobSchedulerNotif {
    private var JOB_ID : Int = 0

    fun registerJob(context: Context, reminder: Reminders, recurring : Boolean = false) {
        JOB_ID = 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(3)
        data.putString("reminderMessage", reminder.message)
        data.putInt("notificationID", JOB_ID)
        data.putBoolean("recurring", recurring)

        val cal = Calendar.getInstance()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            setMinimumLatency(reminder.reminderTime - cal.timeInMillis)
            //setOverrideDeadline(reminder.reminderTime - cal.timeInMillis)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }

    fun weeklyJob(context: Context, message: String){
        JOB_ID = 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(3)
        data.putString("reminderMessage", message)
        data.putInt("notificationID", JOB_ID)
        data.putBoolean("recurring", true)

        val cal = Calendar.getInstance()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            //setMinimumLatency(1000*60*60*24*7) //1week in milliseconds
            //setOverrideDeadline(1000*60*60*24*7)
            setMinimumLatency(1000*60*5)
            //setOverrideDeadline(1000*60)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }

    fun unregisterJob(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(JOB_ID)
        // If you cancel all job
        // scheduler.cancelAll()
    }
}