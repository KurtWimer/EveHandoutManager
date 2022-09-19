package com.example.evehandoutmanager.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import java.text.DateFormat
import java.util.*

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val sharedPreferences = app.getSharedPreferences("EHMPreferences", Context.MODE_PRIVATE)
    private val database = getDatabase(app)
    val accountList = database.accountDao.getAccounts()
    private var accounts : List<Account>? = null //mirrors accountList but as static data instead of LiveData
    private val _handoutList = database.handoutDao.getHandouts()
    val handoutList : LiveData<List<Handout>>
        get() = _handoutList
    private val clientID = app.getString(R.string.client_id)
    private var mostRecentTradeID : Long = sharedPreferences.getLong("mostRecentTradeID", 0)
    private var fleetStartTime : Date? = sharedPreferences.getString("startTime", null)
            ?.let { DateFormat.getDateInstance().parse(it) ?: null}
    var fleetStarted : MutableLiveData<Boolean> = MutableLiveData<Boolean>(fleetStartTime != null)

    fun onRemoveButtonClick(handout: Handout) { database.handoutDao.delete(handout) }

    fun onRemoveAllButtonClick() { database.handoutDao.deleteAll() }

    @SuppressLint("ApplySharedPref")
    fun onStartToggleButtonClick() {
        if (accounts?.isEmpty() == true){
            Toast.makeText(getApplication(), "Please sign in before starting a fleet", Toast.LENGTH_SHORT).show()
            return
        }

        if (fleetStartTime != null){
            fleetStartTime = null
            sharedPreferences.edit().putString("startTime", null).commit()
            fleetStarted.value = false
        } else {
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    database.handoutDao.deleteAll()
                }
            }
            val currentDate: Date = Calendar.getInstance().time
            val currentDateString = DateFormat.getDateInstance().format(currentDate)
            sharedPreferences.edit().putString("startTime", currentDateString).commit()
            fleetStartTime = currentDate
            fleetStarted.value = true
        }
    }

    fun processNewTrades(){
        viewModelScope.launch {
            suspend fun getNewHandouts(trades : List<WalletEntry>): List<Handout> {
                val newHandouts = mutableListOf<Handout>()
                for (trade in trades){
                    val fleetConfig = database.fleetDao.getConfig()
                    var shipName = "Unknown"
                    for (item in fleetConfig){
                        if (item.iskValue == trade.amount) shipName = item.shipName
                    }
                    val receiverName = Esi.retrofitInterface.getCharacter(trade.firstPartyId.toString())
                        .await().name!!
                    newHandouts.add(Handout(trade.id, shipName, receiverName, trade.firstPartyId))
                }
                return newHandouts.toImmutableList()
            }

            //Removes ship returns
            //Assumes each character only gets one Handout, if the character gets multiple it will
            //return the ships in the order they were handed out
            fun processReturns(trades: List<WalletEntry>){
                for (trade in trades){
                    if (trade.id > mostRecentTradeID) {
                        sharedPreferences.edit().apply {
                            putLong("mostRecentTradeID", trade.id)
                        }.apply()
                        mostRecentTradeID = trade.id
                    }
                    val potentialMatches = database.handoutDao.getPlayersHandouts(trade.firstPartyId)
                    when (potentialMatches.size){
                        0 -> Log.d("HomeViewModel", "Unable to find matching trade: $trade")
                        else -> database.handoutDao.delete(potentialMatches.first())
                    }
                }
            }

            withContext(Dispatchers.IO) {
                //Get most recent trade ID to filter out already processed trades
                val tradeID = database.handoutDao.getMostRecentHandout().id
                if (tradeID > mostRecentTradeID){
                    sharedPreferences.edit().apply {
                        putLong("mostRecentTradeID", tradeID)
                    }.apply()
                    mostRecentTradeID = tradeID
                }
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
                        val trades = journal.filter { it.refType == "player_trading" && it.id > mostRecentTradeID && convertDate(it.date).after(fleetStartTime) }
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

    private fun convertDate(date: String): Date { return DateFormat.getDateInstance().parse(date)!! }

    private fun isTokenExpired(accessToken : String): Boolean {
        return requireNotNull(JWT(accessToken).isExpired(0))
    }

    fun updateAccounts(newAccounts: List<Account>) { accounts = newAccounts }
}