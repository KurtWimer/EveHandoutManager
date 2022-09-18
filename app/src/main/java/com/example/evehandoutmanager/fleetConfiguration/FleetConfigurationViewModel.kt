package com.example.evehandoutmanager.fleetConfiguration

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evehandoutmanager.database.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FleetConfigurationViewModel(private val app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)
    var newConfig = FleetConfigItem()

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
                val currentConfig = database.fleetDao.getConfig()
                //Prevent duplicate primary keys
                for (config in currentConfig){
                    if (config.iskValue == item.iskValue){
                        Toast.makeText(app, "Cannot add ship with duplicate ISK value", Toast.LENGTH_LONG).show()
                        return@withContext
                    }
                }
                database.fleetDao.insert(item)
                newConfig = FleetConfigItem()
            }
        }
    }
}