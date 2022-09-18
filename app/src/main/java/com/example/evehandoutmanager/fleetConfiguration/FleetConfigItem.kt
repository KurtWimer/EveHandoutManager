package com.example.evehandoutmanager.fleetConfiguration

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FleetConfigItem (
    @PrimaryKey
    var iskValue: Int = 1,
    var shipName: String = ""
)