package com.upriseus.remindme.features.reminders

import com.google.firebase.database.Exclude

data class Reminders(val message : String,
                     val locationx : Int?=null,
                     val locationy: Int?=null,
                     val reminderTime : Long, // save as timestamp but in milli seconde
                     val creationTime : Long, // save as timestamp but in milli seconde
                     val creatorId : String?=null,
                     val reminderSeen : Boolean?=false)
{
    var uuid: String = ""
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uuid" to uuid,
            "reminderTime" to reminderTime,
            "creationTime" to creationTime,
            "creatorId" to creatorId,
            "message" to message,
            "reminderSeen" to reminderSeen
        )
    }

    companion object {
        fun toObject(map : Map<String, Any?>) : Reminders{
            val r = Reminders(map["message"] as String, reminderTime = map["reminderTime"] as Long, creationTime = map["creationTime"] as Long, creatorId = map["creatorId"] as String, reminderSeen = map["reminderSeen"] as Boolean)
            r.uuid = map["uuid"].toString()
            return r
        }
    }
}