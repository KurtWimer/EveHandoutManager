package com.example.evehandoutmanager.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.auth0.android.jwt.JWT
import com.example.evehandoutmanager.database.getDatabase
import com.example.evehandoutmanager.network.Esi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import retrofit2.await

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)
    private val _accountList = database.accountDao.getAccounts()
    val _HandoutList = database.handoutDao.getHandouts()
    val handoutList : LiveData<List<Handout>>
        get() = _HandoutList

    fun onRemoveButtonClick(handout: Handout) {
        database.handoutDao.delete(handout)
    }

    suspend fun processNewTrades(){
        suspend fun getNewHandouts(trades : List<WalletEntry>): List<Handout> {
            val newHandouts = mutableListOf<Handout>()
            for (trade in trades){
                val shipName = trade.amount.toString() //TODO reference fleet setup to name trade
                val receiverName = Esi.retrofitInterface.getCharacter(trade.secondPartyId.toString())
                    .await().name!!
                newHandouts.add(Handout(trade.id, shipName, receiverName, trade.secondPartyId))
            }
            return newHandouts.toImmutableList()
        }

        fun processReturns(trades: List<WalletEntry>){
            for (trade in trades){
                val potentialMatches = database.handoutDao.getPlayersHandouts(trade.secondPartyId)
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
            val mostRecentTradeID = database.handoutDao.getMostRecentHandout().id
            for (account in _accountList.value!!) {
                if (isTokenExpired(account.AccessToken)) {
                    //TODO update access token
                }
                val journal = Esi.retrofitInterface.getWalletJournal(account.characterID, account.AccessToken).await()
                val trades = journal.filter { it.refType == "player_trading" && it.id > mostRecentTradeID}
                val newHandouts = getNewHandouts(trades.filter { it.amount in 1..9999 })
                processReturns(trades.filter { it.amount == 0 })
                //add all new handouts to DB
                if (newHandouts.isNotEmpty()){
                    database.handoutDao.insert(*newHandouts.toTypedArray())
                }
            }
        }
    }

    private fun isTokenExpired(accessToken : String): Boolean {
        return requireNotNull(JWT(accessToken).isExpired(0))
    }
}