package com.example.evehandoutmanager.accounts

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.auth0.android.jwt.JWT
import com.example.evehandoutmanager.database.LocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.evehandoutmanager.network.Esi
import com.example.evehandoutmanager.network.Sso
import retrofit2.await

@Entity
data class Account constructor(
    @PrimaryKey
    val name : String,
    val iconURL: String,
    val characterID: Int,
    var AccessToken: String,
    var RefreshToken: String){

    suspend fun refreshAccountToken(clientID: String, database: LocalDatabase){
        if (isTokenExpired(AccessToken)) { //Refresh Access Token Before Proceeding
            val self = this
            withContext(Dispatchers.IO) {
                val newToken =
                    Sso.retrofitInterface.refreshAccessToken(RefreshToken, clientID).await()
                AccessToken = newToken.accessToken
                RefreshToken = newToken.refreshToken
                database.accountDao.update(self)
            }
        }
    }

    private fun isTokenExpired(accessToken : String): Boolean {
        return requireNotNull(JWT(accessToken).isExpired(0))
    }
}

//Utility function allows all com.example.evehandoutmanager.network calls to be done before creating a Character object
suspend fun fetchInformation(characterID: String): Pair<String, String> {
    return withContext(Dispatchers.IO){
        val name  = Esi.retrofitInterface.getCharacter(characterID = characterID).await().name
        val iconUrl = Esi.retrofitInterface.getPortrait(characterID = characterID).await().px512x512
        return@withContext Pair(name!!, iconUrl!!)
    }
}