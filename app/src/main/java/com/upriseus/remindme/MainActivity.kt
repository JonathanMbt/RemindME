package com.upriseus.remindme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.upriseus.remindme.features.account.User
import com.upriseus.remindme.features.account.UserActions
import com.upriseus.remindme.features.account.UserListener


class MainActivity : AppCompatActivity(), UserListener {
    private val userActions = UserActions(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLoginStatus()


        val signin = findViewById<Button>(R.id.sign_in)
        val signup = findViewById<Button>(R.id.sign_up)

        signin.setOnClickListener {
            val intent = Intent(applicationContext, SignActivity::class.java)
            intent.putExtra("type", "signin")
            startActivity(intent)
        }

        signup.setOnClickListener {
            val intent = Intent(applicationContext, SignActivity::class.java)
            intent.putExtra("type", "signup")
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(0, 0)
    }

    private fun checkLoginStatus(){
        val loginStatus = applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).getInt("loginStatus", 0)
        if (loginStatus == 1) {
            val username = applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).getString("username", "")
            if (username != null) {
                userActions.getUser(username)
            }
        }
    }

    override fun onUserReceived(user: User) {
        val intent = Intent(applicationContext, HomeActivity::class.java)
            .putExtra("user", user.toMap() as HashMap)
        startActivity(intent)
    }
}