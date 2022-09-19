package com.example.evehandoutmanager

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.evehandoutmanager.home.HomeFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setup Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navFragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
        bottomNavigationView.setupWithNavController(navController)

        //handle callback data from SSO login
        if (intent.data != null) {
            //extracts callback arguments to pass to character fragment
            val uri = Uri.parse(intent.data.toString())
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            val action = HomeFragmentDirections.actionHomeFragmentToAccountFragment(code, state)
            navController.navigate(action)
            Log.v("MainActivity", "Received SSO login callback")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}