package com.upriseus.remindme.features.account

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserActions(val userListener: UserListener) {
    private val DB_NAME = "users"

    private val remindersDb: FirebaseDatabase = Firebase.database("https://remindme-1a400-default-rtdb.firebaseio.com/")
    private val reference = remindersDb.getReference(DB_NAME)

    fun addUser(user : User): String{
        val key = reference.push().key
        if (key != null) {
            reference.child(key).setValue(user)
            return key
        }
        return "first_root"
    }

    fun getUser(username : String) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user = User("", "") //default user
                for (postSnapshot in snapshot.children) {
                    val tmp = User.toObject(postSnapshot.value as HashMap<String, Any?>)
                    tmp.uuid = postSnapshot.key.toString()
                    if(tmp.username == username){
                        user = tmp
                    }
                }
                userListener.onUserReceived(user)
            }


            override fun onCancelled(error: DatabaseError) {
                userListener.onError(error.toException())
            }

        })
    }

    fun updateUser(user: User) {
        reference.child(user.uuid).updateChildren(user.toMap())
    }

    fun deleteUser(user: User) {
        reference.child(user.uuid).removeValue()
        userListener.onUserDeleted(user)
    }
}