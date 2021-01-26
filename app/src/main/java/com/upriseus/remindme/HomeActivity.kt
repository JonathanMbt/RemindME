package com.upriseus.remindme

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.upriseus.remindme.features.Reminders
import com.upriseus.remindme.layout_features.FullScreenDialog
import com.upriseus.remindme.layout_features.MaterialNavActivity
import com.upriseus.remindme.layout_features.ReminderAdapter

class HomeActivity : MaterialNavActivity() {

    private lateinit var reminders : List<Reminders>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lvreminders = findViewById<ListView>(R.id.list_reminders)
        // FIX : code created reminders (test purpose only)
        reminders = listOf(Reminders("First Reminder", "Test"), Reminders("Second Reminder"))
        val adapter  = ReminderAdapter(reminders)
        lvreminders.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.more_reminders)

        fab.setOnClickListener {
            openDialog()
        }
    }

    private fun openDialog() {
        FullScreenDialog.display(supportFragmentManager)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_home
    }

    override fun getItemId(): Int {
        return R.id.home_item
    }
}