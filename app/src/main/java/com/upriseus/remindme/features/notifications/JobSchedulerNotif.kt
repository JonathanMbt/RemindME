package com.upriseus.remindme.features.notifications

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import com.upriseus.remindme.features.reminders.Reminders
import java.util.*
import kotlin.random.Random

object JobSchedulerNotif {
    private val JOB_ID = 23092000 + Random.nextInt(0, 1000)

    fun registerJob(context: Context, reminder: Reminders) {
        JobServiceNotif.reminder = reminder
        JobServiceNotif.NOTIFICATION_ID = JOB_ID
        val cal = Calendar.getInstance()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName)
            .setMinimumLatency(reminder.reminderTime - cal.timeInMillis)
            .setPersisted(true)
            .setRequiresCharging(false)
            .build()
        scheduler.schedule(jobInfo)
    }

    fun unregisterJob(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(JOB_ID)
        // If you cancel all job
        // scheduler.cancelAll()
    }
}