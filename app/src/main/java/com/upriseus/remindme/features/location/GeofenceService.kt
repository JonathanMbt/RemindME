package com.upriseus.remindme.features.location

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import com.google.android.gms.location.Geofence
import com.upriseus.remindme.features.reminders.ReminderListener
import com.upriseus.remindme.features.reminders.Reminders
import com.upriseus.remindme.features.reminders.RemindersActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GeofenceService : JobService(), ReminderListener {
    private lateinit var key : String
    private lateinit var creator : String
    private lateinit var context: Context
    private var geofencingEventTransition : Int = -1
    private var remindersActions = RemindersActions(this)

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("GeoferenceReceiver", "Received")
        context = this
        if (params != null){
            key = params.extras.getString("key").toString()
            creator = params.extras.getString("creator").toString()
            geofencingEventTransition = params.extras.getInt("transition")
        }
        val job = Job()
        CoroutineScope(Dispatchers.Default + job).launch {
            remindersActions.getOwnedReminders(creator)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobFinished(params, false)
        return false
    }


    override fun onReminderReceived(reminder: MutableList<Reminders>) {
        for (remind in reminder){
            if(remind.uuid == key){
                val transition = geofencingEventTransition
                if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                    remind.inGeofence = true
                }else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    remind.inGeofence = false
                }
                remindersActions.updateReminder(remind)
            }
        }
    }

    override fun onError(error: Throwable?) {
        Log.e("HomeActivity : onError", "Get reminders database error")
    }

    override fun onReminderDeleted(reminder: Reminders) {
        //useless
    }
}