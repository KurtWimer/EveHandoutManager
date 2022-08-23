package com.example.evehandoutmanager

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (intent.data != null) {
            //extracts callback arguments to pass to character fragment
            val uri = Uri.parse(intent.data.toString())
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            val callBackArgs = Bundle().apply {
                putString("code", code)
                putString("state", state)
            }
            //creates new navhost to pass initial arguments
            val navFragment = NavHostFragment.create(R.navigation.nav_graph, callBackArgs)
            supportFragmentManager.beginTransaction()
                .replace(R.id.navFragment, navFragment)
                .setPrimaryNavigationFragment(navFragment) // equivalent to app:defaultNavHost="true"
                .commit()
            Log.i("MainActivity", intent.data.toString())
        }else {
            val navFragment = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.beginTransaction()
                .replace(R.id.navFragment, navFragment)
                .setPrimaryNavigationFragment(navFragment) // equivalent to app:defaultNavHost="true"
                .commit()
        }
    }
}