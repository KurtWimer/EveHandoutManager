package com.example.evehandoutmanager.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.accounts.Account
import com.example.evehandoutmanager.database.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.*

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val sharedPreferences = app.getSharedPreferences("EHMPreferences", Context.MODE_PRIVATE)
    private val database = getDatabase(app)
    val accountList = database.accountDao.getAccounts()
    private var accounts : List<Account>? = null //TODO refactor into single account
    private val _handoutList = database.handoutDao.getHandouts()
    val handoutList : LiveData<List<Handout>>
        get() = _handoutList
    private val clientID = app.getString(R.string.client_id)
    //TODO refactor load and save shared preferences based on activity lifecycle
    private var fleetStartTime : Date? = sharedPreferences.getString("startTime", null)?.let { DateFormat.getDateTimeInstance().parse(it) ?: null}
    private var lastWalletFetchTime : Date? = sharedPreferences.getString("walletFetchTime", null)?.let { DateFormat.getDateTimeInstance().parse(it) ?: null}
    private var mostRecentTradeID = 0L //TODO figure out how to handle most recent trade ID
    //End TODO
    var fleetStarted : MutableLiveData<Boolean> = MutableLiveData<Boolean>(fleetStartTime != null)

    fun onFetchTradesButtonClick(){
        val account = accounts?.first() //TODO modify application to only allow one active account
        if (account != null &&
            fleetStartTime != null &&
            (walletUpdateAvailable(sharedPreferences, app = getApplication()))
        ){
            viewModelScope.launch {
                account.refreshAccountToken(clientID, database)
                //TODO do something with returned trade ID
                processNewTrades(
                    database = database,
                    account = account,
                    fleetStartTime = fleetStartTime!!,
                    mostRecentTradeID = mostRecentTradeID
                )
            }
        }
    }

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

    fun updateAccounts(newAccounts: List<Account>) { accounts = newAccounts }
}