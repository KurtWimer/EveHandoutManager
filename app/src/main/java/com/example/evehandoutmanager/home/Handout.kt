package com.example.evehandoutmanager.home

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Handout (
    @PrimaryKey
    val id : Long,
    val shipName : String,
    val receiverName: String,
    val receiverID: Int,
    val receiverIconUrl: String)