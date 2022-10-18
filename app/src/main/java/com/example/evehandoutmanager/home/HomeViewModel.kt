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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
    private var fleetStartTime : Date? = sharedPreferences.getString("startTime", null)?.let { DateFormat.getDateTimeInstance().parse(it) ?: null}
    private var lastWalletFetchTime : Date? = sharedPreferences.getString("walletFetchTime", null)?.let { DateFormat.getDateTimeInstance().parse(it) ?: null}
    var fleetStarted : MutableLiveData<Boolean> = MutableLiveData<Boolean>(fleetStartTime != null)

    fun onRemoveButtonClick(handout: Handout) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.handoutDao.delete(handout)
            }
        }
    }

    fun onRemoveAllButtonClick() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.handoutDao.deleteAll()
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    fun onStartToggleButtonClick() {
        if (accounts?.isEmpty() == true){
            Toast.makeText(getApplication(), "Please sign in before starting a fleet", Toast.LENGTH_SHORT).show()
            return
        }

        if (fleetStartTime != null){ //Stop fleet
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
            val currentDateString = DateFormat.getDateTimeInstance().format(currentDate)
            sharedPreferences.edit().putString("startTime", currentDateString).commit()
            fleetStartTime = currentDate
            fleetStarted.value = true
        }
    }

    fun processNewTrades(){
        //check if new data will be available
        //ESI only allows wallet updates every 30 minutes
        val currentDate: Date = Calendar.getInstance().time
        if(lastWalletFetchTime!= null){
            val timeDiffInMinutes = getDateDiff(lastWalletFetchTime!!, currentDate, TimeUnit.MINUTES)
            val timeUntilNextUpdate = 30 - timeDiffInMinutes
            if(lastWalletFetchTime != null &&  timeDiffInMinutes < 30){
                Toast.makeText(getApplication(), "Next update available in $timeUntilNextUpdate minutes", Toast.LENGTH_SHORT).show()
                return
            }
        }
        else{
            val currentDateString = DateFormat.getDateTimeInstance().format(currentDate)
            sharedPreferences.edit().putString("walletFetchTime", currentDateString).commit()
            lastWalletFetchTime = currentDate
        }
        //Else will get new data and can proceed
        viewModelScope.launch {
            suspend fun getNewHandouts(trades : List<WalletEntry>): List<Handout> {
                val newHandouts = mutableListOf<Handout>()
                for (trade in trades){
                    val fleetConfig = database.fleetDao.getConfig()
                    var shipName = "Unknown"
                    for (item in fleetConfig){
                        if (item.iskValue.toDouble() == trade.amount) shipName = item.shipName
                    }
                    val receiverName = Esi.retrofitInterface.getCharacter(trade.firstPartyId.toString())
                        .await().name!!
                    val receiverIconUrl = Esi.retrofitInterface.getPortrait(characterID = trade.firstPartyId.toString()).await().px128x128!!
                    newHandouts.add(Handout(trade.id, shipName, receiverName, trade.firstPartyId, receiverIconUrl))
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
                val tradeID = database.handoutDao.getMostRecentHandout()?.id ?: 0 //IDE lies this can absolutely return null do NOT refactor
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
                        val newHandouts = getNewHandouts(trades.filter { it.amount.toInt() in 1..9999 })
                        //add all new handouts to DB
                        if (newHandouts.isNotEmpty()){
                            database.handoutDao.insert(*newHandouts.toTypedArray())
                        }
                        processReturns(trades.filter { it.amount == 0.toDouble() })
                    }
                }else{
                    Log.d("HomeViewModel", "No Accounts found $accounts")
                    //This code should never be reached as the button is deactivated if accounts == null
                }
            }
        }
    }

    //Converts From iso-8601 into Date object
    @SuppressLint("SimpleDateFormat")
    private fun convertDate(date: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return format.parse(date)!!
    }

    //shamelessly stolen from https://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
    private fun getDateDiff(date1: Date, date2: Date, timeUnit: TimeUnit): Long {
        val diffInMillies = date2.time - date1.time
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }

    private fun isTokenExpired(accessToken : String): Boolean {
        return requireNotNull(JWT(accessToken).isExpired(0))
    }

    fun updateAccounts(newAccounts: List<Account>) { accounts = newAccounts }
}