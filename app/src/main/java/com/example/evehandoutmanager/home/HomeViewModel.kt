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
        withContext(Dispatchers.IO) {
            for (account in _accountList.value!!) {
                if (isTokenExpired(account.AccessToken)) {
                    //TODO update access token
                }
                val journal = Esi.retrofitInterface.getWalletJournal(account.characterID, account.AccessToken).await()
                val trades = journal.filter { it.refType == "player_trading" }
                var newHandouts = mutableListOf<Handout>()
                //Find all new handouts
                //TODO filter journal to ignore already processed trades
                for (trade in trades.filter { it.amount in 1..9999 }){
                    val shipName = trade.amount.toString() //TODO reference fleet setup to name trade
                    val receiverName = Esi.retrofitInterface.getCharacter(trade.secondPartyId.toString())
                        .await().name!!
                    newHandouts.add(Handout(trade.id, shipName, receiverName, trade.secondPartyId))
                }
                //add all new handouts to DB
                if (newHandouts.isNotEmpty()){
                    database.handoutDao.insert(*newHandouts.toTypedArray())
                }
                //remove all ship return
                for (trade in trades.filter { it.amount == 0 }){
                    val potentialMatches = database.handoutDao.getPlayersHandouts(trade.secondPartyId)
                    when (potentialMatches.size){
                        0 -> Log.d("HomeViewModel", "Unable to find matching trade: ${trade.toString()}")
                        1 -> database.handoutDao.delete(potentialMatches.first())
                        else -> {
                            //TODO handle multiple trades from a single source
                            Log.d("HomeViewModel", "Multiple matching trades")
                        }
                    }
                }
            }
        }
    }

    private fun isTokenExpired(accessToken : String): Boolean {
        return requireNotNull(JWT(accessToken).isExpired(0))
    }
}