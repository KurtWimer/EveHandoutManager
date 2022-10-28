package com.example.evehandoutmanager.accounts

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.auth0.android.jwt.JWT
import com.example.evehandoutmanager.database.AccountDao
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
    var accessToken: String,
    private var _refreshToken: String,
    private var _tradeID: Long? = null){
    val refreshToken: String
        get() = _refreshToken
    val tradeID: Long
        get() = _tradeID?: 0L

    suspend fun refreshAccountToken(clientID: String, dao: AccountDao){
        if (isTokenExpired(accessToken)) { //Refresh Access Token Before Proceeding
            val self = this
            withContext(Dispatchers.IO) {
                val newToken =
                    Sso.retrofitInterface.refreshAccessToken(_refreshToken, clientID).await()
                accessToken = newToken.accessToken
                _refreshToken = newToken.refreshToken
                dao.update(self)
            }
        }
    }

    suspend fun updateTradeID(newID: Long, dao: AccountDao){
        val self = this
        _tradeID = newID
        withContext(Dispatchers.IO){
            dao.update(self)
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