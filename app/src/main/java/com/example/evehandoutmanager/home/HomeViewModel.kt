package com.example.evehandoutmanager.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.evehandoutmanager.database.getDatabase

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)
    val _Handouts = database.handoutDao.getHandouts()


}