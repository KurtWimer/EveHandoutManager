package com.example.evehandoutmanager.fleetConfiguration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.evehandoutmanager.database.getDatabase

class FleetConfigurationViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)

    private val _configList = database.fleetDao.getConfigLive()
    val configList: LiveData<List<FleetConfigItem>>
        get() = _configList


}