package com.example.evehandoutmanager.fleetConfiguration

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class FleetConfigItem (
    @PrimaryKey
    val iskValue: Int,
    val shipName: String
)