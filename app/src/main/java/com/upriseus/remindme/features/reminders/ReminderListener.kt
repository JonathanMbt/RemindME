package com.upriseus.remindme.features.reminders

interface ReminderListener {
    fun onReminderReceived(reminder: MutableList<Reminders>)
    fun onError(error: Throwable?)
    fun onReminderDeleted(reminder: Reminders)
}