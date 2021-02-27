package com.upriseus.remindme

import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.CheckBox
import android.widget.ListView
import android.widget.Toast
import androidx.core.util.remove
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
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

        val username = applicationContext.getSharedPreferences(
                "com.upriseus.remindme",
                MODE_PRIVATE
        ).getString("pseudo", "")!!
        creator = applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).getString(
                "account_id_$username",
                ""
        )!!

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
            val newReminder = Reminders(
                    values["message"]!!,
                    reminderTime = remindTime.timeInMillis,
                    creationTime = cal.timeInMillis,
                    creatorId = creator,
                    notif = values["notif"]?.toBoolean()!!
            )
            if(newReminder.notif){
                if(weekly == true){
                    newReminder.recurring = true
                    newReminder.dayOfWeek = weeklyReminders
                    weeklyReminders?.let { JobSchedulerNotif.weeklyJob(applicationContext, newReminder.message, it) }
                }else{
                    JobSchedulerNotif.registerJob(applicationContext, newReminder)
                }
            }
            newReminder.jobId = JobSchedulerNotif.JOB_ID
            val key = reminderActions.addReminder(newReminder)
            newReminder.uuid = key
        }catch (e: java.lang.Exception){
            Log.e("HomeActivity : userSelectedAValue", "Failed to create reminder")
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
            val upReminder = reminders.find { it.uuid == updatedReminder.uuid }?.copy(
                    message = values["message"]!!,
                    reminderTime = cal.timeInMillis,
                    creationTime = updatedReminder.creationTime,
                    creatorId = creator,
                    dayOfWeek = weeklyReminders,
                    recurring = weekly,
                    notif = values["notif"]?.toBoolean()!!
            )
            upReminder?.uuid = updatedReminder.uuid
            upReminder?.recurring = updatedReminder.recurring
            if (upReminder != null) {
                JobSchedulerNotif.unregisterJob(applicationContext, updatedReminder.jobId)
                if(upReminder.notif){
                    if(weekly == true){
                        upReminder.dayOfWeek?.let { JobSchedulerNotif.weeklyJob(applicationContext, upReminder.message, it) }
                    }else{
                        JobSchedulerNotif.registerJob(applicationContext, upReminder)
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
            }
        }
        reminders.sortBy { it.reminderTime }
        adapter.notifyDataChanged(selectedTab)
    }

    override fun onError(error: Throwable?) {
        Log.e("HomeActivity : onError", "Get reminders database error")
    }

    override fun onReminderDeleted(reminder: Reminders) {
        reminders.remove(reminder)
        JobSchedulerNotif.unregisterJob(applicationContext, reminder.jobId)
        reminders.sortBy { it.reminderTime }
        adapter.notifyDataChanged(selectedTab)
    }

}