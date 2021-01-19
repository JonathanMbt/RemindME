package com.upriseus.remindme

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.upriseus.remindme.features.reminders.ReminderListener
import com.upriseus.remindme.features.reminders.Reminders
import com.upriseus.remindme.features.reminders.RemindersActions
import com.upriseus.remindme.layout_features.DialogListener
import com.upriseus.remindme.layout_features.FullScreenDialog
import com.upriseus.remindme.layout_features.MaterialNavActivity
import com.upriseus.remindme.layout_features.ReminderAdapter
import java.util.*


// INFO : REMINDER DIFFICULT INTERFACE
class HomeActivity : MaterialNavActivity(), DialogListener, ReminderListener {

    private var reminders : MutableList<Reminders> = mutableListOf()
    private lateinit var adapter : ReminderAdapter
    private lateinit var reminderActions : RemindersActions
    private lateinit var creator : String
    /*
    * the var below permits to delete multiple reminders in one time
    * since firebase listener trigger at each value change, when you delete 2 items it will be triggered twice
    * But the problem is that the first trigger, he will get the second element to be deleted (not yet deleted) but in the same moment
    * this element have been removed from the list of reminders(and furthermore from the db), and could without this var be diplayed, even if it wasn't anymore in the database.
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

        reminderActions = RemindersActions(applicationContext, this)

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
                adapter.removeSelection()
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

    override fun userSelectedAValue(values: MutableMap<String, String>) {
        try {
            val cal = Calendar.getInstance()
            val remindTime = Calendar.getInstance()
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
                creatorId = creator
            )
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
                creatorId = creator
            )
            upReminder?.uuid = updatedReminder.uuid
            if (upReminder != null) {
                reminderActions.updateReminder(upReminder)
                reminders.add(upReminder)
                reminders.remove(updatedReminder)
                reminders.sortBy { it.reminderTime }
            }
            adapter.notifyDataSetChanged()
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
        adapter.notifyDataSetChanged()
    }

    override fun onError(error: Throwable?) {
        Log.e("HomeActivity : onError", "Get reminders database error")
    }

    override fun onReminderDeleted(reminder: Reminders) {
        reminders.remove(reminder)
        reminders.sortBy { it.reminderTime }
        adapter.notifyDataSetChanged()
    }

}