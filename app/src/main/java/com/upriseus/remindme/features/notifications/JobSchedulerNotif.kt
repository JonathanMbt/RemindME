package com.upriseus.remindme.features.notifications

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.upriseus.remindme.features.reminders.Reminders
import java.util.*
import kotlin.random.Random

object JobSchedulerNotif {
    var JOB_ID : Int = 0

    private fun getCreator(context: Context) : String{
        val username = context.getSharedPreferences(
                "com.upriseus.remindme",
                AppCompatActivity.MODE_PRIVATE
        ).getString("pseudo", "")!!
        return context.getSharedPreferences("com.upriseus.remindme", AppCompatActivity.MODE_PRIVATE).getString(
                "account_id_$username",
                ""
        )!!
    }

    fun registerJob(context: Context, reminder: Reminders) {
        JOB_ID = 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(2)
        data.putInt("notificationID", JOB_ID)
        data.putString("key", reminder.uuid)
        data.putString("creator", getCreator(context))

        val cal = Calendar.getInstance()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        if(reminder.reminderTime != null){
            val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
                setMinimumLatency(reminder.reminderTime - cal.timeInMillis)
                setPersisted(true)
                setExtras(data)
            }.build()

            scheduler.schedule(jobInfo)
        }
    }

    fun instantJob(context: Context, reminder: Reminders) {
        JOB_ID = 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(2)
        data.putInt("notificationID", JOB_ID)
        data.putString("key", reminder.uuid)
        data.putString("creator", getCreator(context))

        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            setMinimumLatency(0)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }


    fun weeklyJob(context: Context, reminder: Reminders, dayOfWeek: Int, jobId: Int? = null){
        JOB_ID = jobId ?: 23092000 + Random.nextInt(0, 1000)
        val data = PersistableBundle(2)
        data.putInt("notificationID", JOB_ID)
        data.putString("key", "")
        data.putString("creator", getCreator(context))

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

    fun snoozeJob(context: Context, reminder: Reminders, snoozeTime : Long, jobId: Int){
        JOB_ID = jobId
        val data = PersistableBundle(2)
        data.putInt("notificationID", JOB_ID)
        data.putString("key", reminder.uuid)
        data.putString("creator", getCreator(context))

        val cal = Calendar.getInstance()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, JobServiceNotif::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            setMinimumLatency(cal.timeInMillis + snoozeTime - cal.timeInMillis)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }

    fun unregisterJob(context: Context, jobId: Int) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(jobId)
    }

    fun unregisterAllJob(context: Context){
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancelAll()
    }
}