package com.example.evehandoutmanager.accounts

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.evehandoutmanager.R
import com.example.evehandoutmanager.database.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.evehandoutmanager.network.ESIRepo

class AccountViewModel (private val app: Application) : AndroidViewModel(app){
    private val sharedPreferences = app.getSharedPreferences("EHMPreferences", MODE_PRIVATE)

    private val _navigateToSSO = MutableLiveData<Intent?>(null)
    val navigateToSSO : LiveData<Intent?>
        get() = _navigateToSSO

    private val database = getDatabase(app)

    //currently logged in Characters
    private val _accountList = database.accountDao.getAccounts()
    val accountList: LiveData<List<Account>>
        get() = _accountList

    //sets flag to trigger navigation
    fun onLoginButton(){
        val (challenge, verifier) = ESIRepo.generateChallenge()
        sharedPreferences.edit().apply {
            putString("verifier", verifier)
        }.commit()
        val intent = ESIRepo.getLoginIntent(
            app.getString(R.string.client_id),
            app.getString(R.string.redirect_uri),
            app.getString(R.string.state),
            challenge
        )
        _navigateToSSO.value = intent
    }
    //called after navigating
    fun onLoginButtonComplete(){ _navigateToSSO.value = null }

    fun handleCallback(code: String) {
        viewModelScope.launch {
            val clientID = app.getString(R.string.client_id)
            //get a token from ESI
            val token = ESIRepo.handleCallback(clientID, code, requireNotNull(sharedPreferences.getString("verifier", null)))
            if (token.validate()){
                Log.i("CharacterViewModel", "received authorization token")
                val (name, iconUrl) = fetchInformation(token.charcterID)
                val newChar = Account(name, iconUrl, token.charcterID.toInt(), token.accessToken, token.refreshToken)
                withContext(Dispatchers.IO){
                    database.accountDao.insert(newChar)
                }
            }else{
                Log.w("CharacterViewModel", "Invalid Token Received: $token")
            }

        }
    }

    fun onLogoutButtonClicked(account: Account) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.accountDao.delete(account)
            }
        }
    }
}