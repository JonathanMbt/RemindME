package com.upriseus.remindme.features.account

import android.util.Log


interface UserListener {
    fun onUserReceived(user: User)
    fun onError(error: Throwable?) { Log.e("User", "Error while using user db with firebase")}
    fun onUserDeleted(user: User) { }
}