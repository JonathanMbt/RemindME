package com.upriseus.remindme.features.location

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.upriseus.remindme.features.notifications.JobSchedulerNotif
import com.upriseus.remindme.features.notifications.JobServiceNotif
import com.upriseus.remindme.features.reminders.Reminders
import java.time.zone.ZoneOffsetTransition
import java.util.*
import kotlin.random.Random

object JobSchedulerGeofence {
    var JOB_ID : Int = 0

    fun registerJob(context: Context, key : String, transition: Int, creator : String) {
        JOB_ID = 23092000 + Random.nextInt(1001, 2000)
        val data = PersistableBundle(3)
        data.putString("key", key)
        data.putString("creator", creator)
        data.putInt("transition", transition)

        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(context, GeofenceService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID, componentName).apply {
            setMinimumLatency(0)
            setPersisted(true)
            setExtras(data)
        }.build()

        scheduler.schedule(jobInfo)
    }
}