package com.upriseus.remindme.features.notifications

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log
import com.upriseus.remindme.features.reminders.Reminders
import java.time.DayOfWeek
import java.util.*
import kotlin.random.Random

object JobSchedulerNotif {
    var JOB_ID : Int = 0

    fun registerJob(context: Context, reminder: Reminders) {
        JOB_ID = 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(3)
        data.putString("reminderMessage", reminder.message)
        data.putInt("notificationID", JOB_ID)
        data.putBoolean("recurring", false)
        data.putInt("dayOfWeek", 10) //unvalid value as reminder is not recurring

        val cal = Calendar.getInstance()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            setMinimumLatency(reminder.reminderTime - cal.timeInMillis)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }

    fun weeklyJob(context: Context, message: String, dayOfWeek: Int){
        JOB_ID = 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(4)
        data.putString("reminderMessage", message)
        data.putInt("notificationID", JOB_ID)
        data.putBoolean("recurring", true)
        data.putInt("dayOfWeek", dayOfWeek)

        val cal = Calendar.getInstance()
        val actualTime = cal.timeInMillis
        while(cal.get( Calendar.DAY_OF_WEEK ) != dayOfWeek){
            cal.add( Calendar.DATE, 1 )
        }
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            setMinimumLatency(cal.timeInMillis - actualTime)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }

    fun unregisterJob(context: Context, jobId: Int) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(jobId)
    }
}