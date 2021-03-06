package com.upriseus.remindme.features.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.upriseus.remindme.features.reminders.ReminderListener
import com.upriseus.remindme.features.reminders.Reminders
import com.upriseus.remindme.features.reminders.RemindersActions
import java.util.*

class GeofenceReceiver : BroadcastReceiver() {
    private lateinit var key : String
    private lateinit var creator : String
    private lateinit var geofencingEvent : GeofencingEvent

    override fun onReceive(context: Context?, intent: Intent?) {

        val params = intent?.extras
        if (params != null){
            geofencingEvent = GeofencingEvent.fromIntent(intent)
            creator = params.getString("creator").toString()
            key = params.getString("key").toString()
            if (context != null) {
                JobSchedulerGeofence.registerJob(context, key, geofencingEvent.geofenceTransition, creator)
            }
        }
    }


}
