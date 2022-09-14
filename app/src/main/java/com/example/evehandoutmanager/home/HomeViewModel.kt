package com.example.evehandoutmanager.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.JWT
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.accounts.Account
import com.example.evehandoutmanager.database.getDatabase
import com.example.evehandoutmanager.network.Esi
import com.example.evehandoutmanager.network.Sso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import retrofit2.await

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)
    val accountList = database.accountDao.getAccounts()
    private var accounts : List<Account>? = null //mirrors accountList but as static data instead of LiveData
    private val _HandoutList = database.handoutDao.getHandouts()
    val handoutList : LiveData<List<Handout>>
        get() = _HandoutList
    private val clientID = app.getString(R.string.client_id)

    fun onRemoveButtonClick(handout: Handout) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                database.handoutDao.delete(handout)
            }
        }
    }

    fun processNewTrades(){
        viewModelScope.launch {
            suspend fun getNewHandouts(trades : List<WalletEntry>): List<Handout> {
                val newHandouts = mutableListOf<Handout>()
                for (trade in trades){
                    val shipName = trade.amount.toString() //TODO reference fleet setup to name trade
                    val receiverName = Esi.retrofitInterface.getCharacter(trade.firstPartyId.toString())
                        .await().name!!
                    newHandouts.add(Handout(trade.id, shipName, receiverName, trade.firstPartyId))
                }
                return newHandouts.toImmutableList()
            }

            fun processReturns(trades: List<WalletEntry>){
                for (trade in trades){
                    val potentialMatches = database.handoutDao.getPlayersHandouts(trade.firstPartyId)
                    when (potentialMatches.size){
                        0 -> Log.d("HomeViewModel", "Unable to find matching trade: $trade")
                        1 -> database.handoutDao.delete(potentialMatches.first())
                        else -> {
                            //TODO handle multiple trades from a single source
                            Log.d("HomeViewModel", "Multiple matching trades")
                        }
                    }
                }
            }

            withContext(Dispatchers.IO) {
                //Get most recent trade ID to filter out already processed trades
                val mostRecentTradeID = database.handoutDao.getMostRecentHandout()?.id ?: 0
                if (accounts != null){
                    for (account in accounts!!) {
                        if (isTokenExpired(account.AccessToken)) { //Refresh Access Token Before Proceeding
                            val newToken = Sso.retrofitInterface.refreshAccessToken(account.RefreshToken, clientID).await()
                            account.AccessToken = newToken.accessToken
                            account.RefreshToken = newToken.refreshToken
                            database.accountDao.update(account)
                        }
                        //Get and filter all wallet transaction to find ship handouts
                        val journal = Esi.retrofitInterface.getWalletJournal(account.characterID.toString(), account.AccessToken).await()
                        val trades = journal.filter { it.refType == "player_trading" && it.id > mostRecentTradeID}
                        val newHandouts = getNewHandouts(trades.filter { it.amount in 1..9999 })
                        //add all new handouts to DB
                        if (newHandouts.isNotEmpty()){
                            database.handoutDao.insert(*newHandouts.toTypedArray())
                        }
                        processReturns(trades.filter { it.amount == 0 })
                    }
                }else{
                    Log.d("HomeViewModel", "No Accounts found $accounts")
                    //TODO warn user to login
                }
            }
        }
    }

    private fun isTokenExpired(accessToken : String): Boolean {
        return requireNotNull(JWT(accessToken).isExpired(0))
    }

    fun updateAccounts(newAccounts: List<Account>) { accounts = newAccounts }
}