package com.upriseus.remindme.layout_features

import com.upriseus.remindme.features.reminders.Reminders


interface DialogListener {
    fun userSelectedAValue(values: MutableMap<String, String>)
    fun userUpdatedAValue(values: MutableMap<String, String>, updatedReminder:Reminders)
    fun userCanceled()
}

