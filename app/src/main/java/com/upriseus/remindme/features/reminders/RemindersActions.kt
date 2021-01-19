package com.upriseus.remindme.features.reminders

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RemindersActions(context: Context, val reminderListener: ReminderListener) : ViewModel() {
    private val DB_NAME = "reminders"

    private val remindersDb: FirebaseDatabase = Firebase.database("https://remindme-1a400-default-rtdb.firebaseio.com/")
    private val reference = remindersDb.getReference(DB_NAME)

    fun addReminder(reminder: Reminders): String {
        val key = reference.push().key
        if (key != null) {
            reference.child(key).setValue(reminder)
            return key
        }
        return "first_root"
    }

    fun getOwnedReminders(id: String){
        var r : MutableList<Reminders>
        var reminder : Reminders
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                r = mutableListOf<Reminders>()
                for (postSnapshot in snapshot.children) {
                    reminder = Reminders.toObject(postSnapshot.value as HashMap<String, Any?>)
                    reminder.uuid = postSnapshot.key.toString()
                    if ((reminder.creatorId == id)) {
                        r.add(reminder)
                    }
                }
                reminderListener.onReminderReceived(r)
            }


            override fun onCancelled(error: DatabaseError) {
                reminderListener.onError(error.toException())
            }

        })
    }

    fun updateReminder(reminder: Reminders) {
        reference.child(reminder.uuid).updateChildren(reminder.toMap())
    }

    fun deleteReminder(reminder: Reminders) {
        reference.child(reminder.uuid).removeValue()
        reminderListener.onReminderDeleted(reminder)
    }

}