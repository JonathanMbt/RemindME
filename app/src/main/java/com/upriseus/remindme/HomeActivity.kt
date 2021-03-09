package com.upriseus.remindme

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.CheckBox
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.upriseus.remindme.features.account.User
import com.upriseus.remindme.features.location.GeofenceReceiver
import com.upriseus.remindme.features.notifications.JobSchedulerNotif
import com.upriseus.remindme.features.reminders.ReminderListener
import com.upriseus.remindme.features.reminders.Reminders
import com.upriseus.remindme.features.reminders.RemindersActions
import com.upriseus.remindme.layout_features.DialogListener
import com.upriseus.remindme.layout_features.FullScreenDialog
import com.upriseus.remindme.layout_features.MaterialNavActivity
import com.upriseus.remindme.layout_features.ReminderAdapter
import java.util.*


class HomeActivity : MaterialNavActivity(), DialogListener, ReminderListener {

    private var reminders : MutableList<Reminders> = mutableListOf()
    private var weeklyReminders : Int? = null //key 1 is sunday, key 2 is Monday...
    private lateinit var adapter : ReminderAdapter
    private lateinit var reminderActions : RemindersActions
    private lateinit var creator : String
    private var selectedTab = 0
    private lateinit var geofencingClient: GeofencingClient

    /*
    * the var below permits to delete multiple reminders in one time
    * since firebase listener trigger at each value change, when you delete 2 items it will be triggered twice
    * But the problem is that: the first trigger, it will get the second element to be deleted (not yet deleted)So it will be displayed.
    * But, this element already have been removed from the list of reminders(and furthermore from the db),
    *  and could without be displayed, even if it wasn't anymore in the database.
    * */
    private var alreadyDisplayedReminders = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = User.toObject(intent.extras?.getSerializable("user") as Map<String, Any?>)

        creator = user.uuid
        JobSchedulerNotif.CREATOR = creator
        val lvreminders = findViewById<ListView>(R.id.list_reminders)

        reminderActions = RemindersActions(this)

        adapter  = ReminderAdapter(reminders)
        lvreminders.adapter = adapter

        reminderActions.getOwnedReminders(creator)

        val fab = findViewById<FloatingActionButton>(R.id.more_reminders)

        fab.setOnClickListener {
            openDialog()
        }
        // INFO : Based on this topic : https://stackoverflow.com/questions/22554853/selecting-multiple-items-in-list-using-setonitemlongclicklistener
        lvreminders.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lvreminders.setMultiChoiceModeListener(object : MultiChoiceModeListener {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.context_action_bar_reminders, menu);
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                try {
                    return when (item!!.itemId) {
                        R.id.delete -> {
                            val selected = adapter.getSelectedIds()
                            for (i in selected.size() - 1 downTo 0) {
                                if (selected.valueAt(i)) {
                                    val selectedItem: Reminders = adapter.getItem(selected.keyAt(i))
                                    reminderActions.deleteReminder(selectedItem)
                                }
                            }
                            mode?.finish()
                            true
                        }
                        else -> {
                            false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeActivity : OnActionItemClicked", "failed to delete selected items")
                    return false
                }
            }


            override fun onDestroyActionMode(mode: ActionMode?) {
                adapter.removeSelection(selectedTab)
            }

            override fun onItemCheckedStateChanged(
                    mode: ActionMode?,
                    position: Int,
                    id: Long,
                    checked: Boolean
            ) {
                mode?.title = "${lvreminders.checkedItemCount} selected"
                adapter.toggleSelection(position)
            }

        })

        lvreminders.setOnItemClickListener { _, _, position, _ ->
            FullScreenDialog.displayUpdate(supportFragmentManager, this, adapter.getItem(position))
        }

        val tabLayout = findViewById<TabLayout>(R.id.toolbar_tab)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    /* use tab position as tab id doesn't work and always return -1
                    * More on this at: https://github.com/material-components/material-components-android/issues/1162
                    * */
                    when (tab.position) {
                        1 -> {
                            selectedTab = 1
                        }
                        0 -> {
                            selectedTab = 0
                        }
                    }
                    adapter.notifyDataChanged(selectedTab)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // INFO : Not Needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // INFO : Not Needed
            }

        })

        geofencingClient = LocationServices.getGeofencingClient(applicationContext)
    }

    private fun openDialog() {
        FullScreenDialog.display(supportFragmentManager, this)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_home
    }

    override fun getItemId(): Int {
        return R.id.home_item
    }

    fun weekListener(view: View){
        val chbx = view as CheckBox
        when(chbx.id){
            R.id.monday -> {
                weeklyReminders = if (chbx.isChecked) {
                    2
                } else {
                    null
                }
            }
            R.id.tuesday -> {
                weeklyReminders = if (chbx.isChecked) {
                    3
                } else {
                    null
                }
            }
            R.id.wednesday -> {
                weeklyReminders = if (chbx.isChecked) {
                    4
                } else {
                    null
                }
            }
            R.id.thursday -> {
                weeklyReminders = if (chbx.isChecked) {
                    5
                } else {
                    null
                }
            }
            R.id.friday -> {
                weeklyReminders = if (chbx.isChecked) {
                    6
                } else {
                    null
                }
            }
            R.id.saturday -> {
                weeklyReminders = if (chbx.isChecked) {
                    7
                } else {
                    null
                }
            }
            R.id.sunday -> {
                weeklyReminders = if (chbx.isChecked) {
                    1
                } else {
                    null
                }
            }
        }
    }

    override fun userSelectedAValue(values: MutableMap<String, String>) {
        try {
            lateinit var newReminder: Reminders
            val cal = Calendar.getInstance()
            val remindTime = Calendar.getInstance()
            val weekly = values["recurring"]?.toBoolean()
            remindTime.set(
                    Integer.parseInt(values["year"]!!),
                    (Integer.parseInt(values["month"]!!)),
                    Integer.parseInt(
                            values["day"]!!
                    ),
                    Integer.parseInt(values["hours"]!!),
                    Integer.parseInt(values["minutes"]!!), 0)
            if(values["locationx"] != "null" && values["locationy"] != "null"){
                if(values["timeBased"]?.toBoolean() == true){ //timeBased and locationBased
                    newReminder = Reminders(
                            values["message"]!!,
                            reminderTime = remindTime.timeInMillis,
                            creationTime = cal.timeInMillis,
                            creatorId = creator,
                            notif = values["notif"]?.toBoolean()!!,
                            locationx = values["locationx"]?.toDouble(),
                            locationy = values["locationy"]?.toDouble()
                    )
                }else{ //locationBased only
                    newReminder = Reminders(
                            values["message"]!!,
                            reminderTime = null,
                            creationTime = cal.timeInMillis,
                            creatorId = creator,
                            notif = values["notif"]?.toBoolean()!!,
                            locationx = values["locationx"]?.toDouble(),
                            locationy = values["locationy"]?.toDouble()
                    )
                }
            }else{ //timeBased only
                newReminder = Reminders(values["message"]!!,
                        reminderTime = remindTime.timeInMillis,
                        creationTime = cal.timeInMillis,
                        creatorId = creator,
                        notif = values["notif"]?.toBoolean()!!)
            }
            val key = reminderActions.addReminder(newReminder)
            newReminder.uuid = key
            if(newReminder.notif){
                if(weekly == true){
                    newReminder.recurring = true
                    newReminder.dayOfWeek = weeklyReminders
                    weeklyReminders?.let { JobSchedulerNotif.weeklyJob(applicationContext, newReminder,it)  }
                }else{
                    if(newReminder.locationx != null && newReminder.locationy != null){
                        createGeoFence(geofencingClient, newReminder)
                    }else{
                        JobSchedulerNotif.registerJob(applicationContext, newReminder)
                    }
                }
            }
            newReminder.jobId = JobSchedulerNotif.JOB_ID
            reminderActions.updateReminder(newReminder)
        }catch (e: java.lang.Exception){
            Log.e("HomeActivity : userSelectedAValue", "Failed to create reminder")
            e.localizedMessage?.let { Log.e("HomeActivity : userSelectedAValue", it) }
            Toast.makeText(applicationContext, "Failed to create reminder", Toast.LENGTH_LONG).show()
        }

    }

    override fun userUpdatedAValue(values: MutableMap<String, String>, updatedReminder: Reminders) {
        try {
            val cal = Calendar.getInstance()
            val weekly = values["recurring"]?.toBoolean()
            cal.set(
                    Integer.parseInt(values["year"]!!),
                    Integer.parseInt(values["month"]!!),
                    Integer.parseInt(values["day"]!!),
                    Integer.parseInt(values["hours"]!!),
                    Integer.parseInt(values["minutes"]!!), 0)
            var upReminder = reminders.find { it.uuid == updatedReminder.uuid }
            if(upReminder != null){
                if(values["timeBased"]?.toBoolean() != false){
                    upReminder = upReminder.copy(
                            message = values["message"]!!,
                            reminderTime = cal.timeInMillis,
                            creationTime = updatedReminder.creationTime,
                            creatorId = creator,
                            dayOfWeek = weeklyReminders,
                            recurring = weekly,
                            notif = values["notif"]?.toBoolean()!!,
                            locationx = values["locationx"]?.toDouble(),
                            locationy = values["locationy"]?.toDouble()
                    )
                }else{
                    upReminder = upReminder.copy(
                            message = values["message"]!!,
                            reminderTime = null,
                            creationTime = updatedReminder.creationTime,
                            creatorId = creator,
                            dayOfWeek = weeklyReminders,
                            recurring = weekly,
                            notif = values["notif"]?.toBoolean()!!,
                            locationx = values["locationx"]?.toDouble(),
                            locationy = values["locationy"]?.toDouble()
                    )
                }
            }
            upReminder?.uuid = updatedReminder.uuid
            upReminder?.recurring = updatedReminder.recurring
            if (upReminder != null) {
                JobSchedulerNotif.unregisterJob(applicationContext, updatedReminder.jobId)
                if(upReminder.notif){
                    if(weekly == true){
                        upReminder.dayOfWeek?.let { JobSchedulerNotif.weeklyJob(applicationContext, upReminder, it) }
                    }else{
                        if(updatedReminder.locationx != null && updatedReminder.locationy != null){
                            removeGeofences(applicationContext, mutableListOf(updatedReminder.uuid))
                        }
                        if(upReminder.locationx != null && upReminder.locationy != null){
                            createGeoFence(geofencingClient, upReminder)
                        }else{
                            JobSchedulerNotif.registerJob(applicationContext, upReminder)
                        }
                    }
                }
                upReminder.jobId = JobSchedulerNotif.JOB_ID
                reminderActions.updateReminder(upReminder)
                reminders.add(upReminder)
                reminders.remove(updatedReminder)
                reminders.sortBy { it.reminderTime }
            }
            adapter.notifyDataChanged(selectedTab)
        }catch (e: java.lang.Exception){
            Log.e("HomeActivity : userSelectedAValue", "Failed to create reminder")
            Toast.makeText(applicationContext, "Failed to create reminder", Toast.LENGTH_LONG).show()
        }
    }

    override fun userCanceled() {
        // INFO : Not needed but could be useful ^^'
    }

    override fun onReminderReceived(reminder: MutableList<Reminders>) {
        for (remind in reminder){
            if(remind.uuid !in alreadyDisplayedReminders){
                reminders.add(remind)
                alreadyDisplayedReminders.add(remind.uuid)
                if(JobSchedulerNotif.getJob(applicationContext, remind.jobId) == null){
                    initializeReminders(remind)
                }
            }
        }
        reminders.sortBy { it.reminderTime }
        adapter.notifyDataChanged(selectedTab)
    }

    private fun initializeReminders(reminder: Reminders) {
        if(reminder.locationx == null){ //time based reminders
            if(reminder.recurring == true){ //weekly reminders
                reminder.dayOfWeek?.let {
                    JobSchedulerNotif.weeklyJob(applicationContext, reminder, it)
                }
            }else{ //unique Reminder
                JobSchedulerNotif.registerJob(applicationContext, reminder)
            }
        }else{ // location based reminders
            createGeoFence(geofencingClient, reminder)
        }
    }

    override fun onError(error: Throwable?) {
        Log.e("HomeActivity : onError", "Get reminders database error")
    }

    override fun onReminderDeleted(reminder: Reminders) {
        reminders.remove(reminder)
        JobSchedulerNotif.unregisterJob(applicationContext, reminder.jobId)
        removeGeofences(applicationContext, mutableListOf(reminder.uuid))
        reminders.sortBy { it.reminderTime }
        adapter.notifyDataChanged(selectedTab)
    }


    private fun createGeoFence(geofencingClient: GeofencingClient, reminder: Reminders) {
        val key = reminder.uuid
        val location = LatLng(reminder.locationy!!, reminder.locationx!!)
        val geofence = Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(location.latitude, location.longitude, 100.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if(reminder.reminderTime != null){
            JobSchedulerNotif.registerJob(applicationContext, reminder)
        }


        val intent = Intent(applicationContext, GeofenceReceiver::class.java)
            .putExtra("key", key)
            .putExtra("creator", creator)

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                            applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),5)
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 5) {
            if (permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this,
                        "This application needs background location to work on Android 10 and higher",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removeGeofences(context: Context, triggeringGeofenceListId: MutableList<String>) {
        LocationServices.getGeofencingClient(context).removeGeofences(triggeringGeofenceListId)
    }
}