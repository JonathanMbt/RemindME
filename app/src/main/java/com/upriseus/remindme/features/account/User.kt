package com.upriseus.remindme.features.account

import com.google.firebase.database.Exclude


data class User(val username : String, val password : String) {
    var uuid = ""

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
                "username" to username,
                "uuid" to uuid,
                "password" to password
        )
    }

    companion object {
        fun toObject(map : Map<String, Any?>) : User{
            val user = User(map["username"] as String, map["password"] as String)
            user.uuid = map["uuid"] as String
            return user
        }
    }
}