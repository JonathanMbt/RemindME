package com.upriseus.remindme.features.reminders

import com.google.firebase.database.Exclude

data class Reminders(val message : String,
                     val locationx : Double?=null,
                     val locationy: Double?=null,
                     val reminderTime : Long? = null, // save as timestamp but in milli seconde
                     val creationTime : Long, // save as timestamp but in milli seconde
                     val creatorId : String?=null,
                     val reminderSeen : Boolean?=false, var recurring : Boolean?=false, var dayOfWeek: Int?=null, var jobId :Int = 0, var notif : Boolean = true, var inGeofence : Boolean = false)
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
            "jobId" to jobId.toLong(),
            "notif" to notif,
            "locationx" to locationx,
            "locationy" to locationy,
            "inGeofence" to inGeofence
        )
    }

    companion object {
        fun toObject(map : Map<String, Any?>) : Reminders{
            val r = Reminders(map["message"] as String, reminderTime = map["reminderTime"] as Long?,
                    creationTime = map["creationTime"] as Long, creatorId = map["creatorId"] as String,
                    reminderSeen = map["reminderSeen"] as Boolean, recurring = map["recurring"] as Boolean,
                    dayOfWeek = (map["dayOfWeek"] as Long?)?.toInt(), jobId = (map["jobId"] as Long).toInt(),
                    notif = map["notif"] as Boolean, locationx = map["locationx"] as Double?, locationy = map["locationy"] as Double?, inGeofence = map["inGeofence"]as Boolean)
            r.uuid = map["uuid"].toString()
            return r
        }
    }
}