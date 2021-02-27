package com.upriseus.remindme.features.reminders

import com.google.firebase.database.Exclude
import java.time.DayOfWeek

data class Reminders(val message : String,
                     val locationx : Int?=null,
                     val locationy: Int?=null,
                     val reminderTime : Long, // save as timestamp but in milli seconde
                     val creationTime : Long, // save as timestamp but in milli seconde
                     val creatorId : String?=null,
                     val reminderSeen : Boolean?=false, var recurring : Boolean?=false, var dayOfWeek: Int?=null, var jobId :Int = 0)
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
            "reminderSeen" to reminderSeen, "recurring" to recurring,"dayOfWeek" to dayOfWeek,
            "jobId" to jobId
        )
    }

    companion object {
        fun toObject(map : Map<String, Any?>) : Reminders{
            val r = Reminders(map["message"] as String, reminderTime = map["reminderTime"] as Long, creationTime = map["creationTime"] as Long, creatorId = map["creatorId"] as String, reminderSeen = map["reminderSeen"] as Boolean, recurring = map["recurring"] as Boolean, dayOfWeek = (map["dayOfWeek"] as Long?)?.toInt(), jobId = (map["jobId"] as Long).toInt())
            r.uuid = map["uuid"].toString()
            return r
        }
    }
}