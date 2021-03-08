package com.upriseus.remindme.layout_features

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.upriseus.remindme.HomeActivity
import com.upriseus.remindme.MainActivity
import com.upriseus.remindme.R

abstract class MaterialNavActivity: AppCompatActivity() {

    private lateinit var drawer : DrawerLayout
    private lateinit var drawerview : NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this.getLayoutId())

        val navbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        this.drawer = findViewById(R.id.drawer)
        this.drawerview = findViewById(R.id.drawerView)


        navbar.setNavigationOnClickListener {
            when(drawer.isDrawerOpen(GravityCompat.START)){
                true-> drawer.closeDrawer(GravityCompat.START)
                false -> drawer.openDrawer(GravityCompat.START)
            }
        }

        navbar.setOnMenuItemClickListener {
            // TODO : If item menu add the navigation/feature
            when(it.itemId){
                else -> false
            }
        }


        this.drawerview.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home_item -> {
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.logout_item -> {
                    applicationContext.getSharedPreferences("com.upriseus.remindme", MODE_PRIVATE).edit().putInt("loginStatus", 0).apply()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    override fun onStart() {
        super.onStart()
        this.drawerview.setCheckedItem(getItemId()) //set the right item to check
        overridePendingTransition(0, 0) //remove transition animation between menu item activities
    }


    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    abstract fun getLayoutId() : Int //return Layout id

    abstract fun getItemId() : Int //return item id in navigation drawer
}