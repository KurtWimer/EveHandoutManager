package com.example.evehandoutmanager.fleetConfiguration

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FleetConfigItem (
    @PrimaryKey
    val iskValue: Int = 0,
    val shipName: String = ""
)