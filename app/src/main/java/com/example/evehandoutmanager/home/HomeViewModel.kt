package com.example.evehandoutmanager.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.evehandoutmanager.database.getDatabase

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)
    val _HandoutList = database.handoutDao.getHandouts()
    val handoutList : LiveData<List<Handout>>
        get() = _HandoutList

    fun onRemoveButtonClick(handout: Handout) {
        database.handoutDao.delete(handout)
    }
}