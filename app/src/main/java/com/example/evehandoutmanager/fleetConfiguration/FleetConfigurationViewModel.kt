package com.example.evehandoutmanager.fleetConfiguration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evehandoutmanager.database.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FleetConfigurationViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)

    val configList = database.fleetDao.getConfigLive()

    fun onRemoveItem(item: FleetConfigItem){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.fleetDao.delete(item)
            }
        }
    }

    fun onAddNewClick(item: FleetConfigItem){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.fleetDao.insert(item)
            }
        }
    }
}