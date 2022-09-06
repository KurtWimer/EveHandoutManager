package com.example.evehandoutmanager.accounts

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.evehandoutmanager.network.Esi
import retrofit2.await

@Entity
data class Account constructor(
    @PrimaryKey
    val name : String,
    val iconURL: String,
    val AccessToken: String,
    val RefreshToken: String)

//Utility function allows all com.example.evehandoutmanager.network calls to be done before creating a Character object
suspend fun fetchInformation(characterID: String): Pair<String, String> {
    return withContext(Dispatchers.IO){
        val name  = Esi.retrofitInterface.getCharacter(characterID = characterID).await().name
        val iconUrl = Esi.retrofitInterface.getPortrait(characterID = characterID).await().px512x512//TODO dynamically choose size of icon
        return@withContext Pair(name!!, iconUrl!!)
    }
}