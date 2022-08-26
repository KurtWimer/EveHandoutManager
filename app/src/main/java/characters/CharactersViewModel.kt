package characters

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.evehandoutmanager.R
import kotlinx.coroutines.launch
import network.ESIRepo

class CharactersViewModel (private val app: Application, private val state : SavedStateHandle)
        : AndroidViewModel(app){
    private val sharedPreferences = app.getSharedPreferences("EHMPreferences", MODE_PRIVATE)

    private val _navigateToSSO = MutableLiveData<Intent?>(null)
    val navigateToSSO : LiveData<Intent?>
        get() = _navigateToSSO

    //private val dataSource TODO

    //currently logged in Characters
    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>>
        get() = _characterList


    //sets flag to trigger navigation
    fun onLoginButton(){
        val (challenge, verifier) = ESIRepo.generateChallenge()
        sharedPreferences.edit().apply {
            putString("verifier", verifier)
        }.commit()
        requireNotNull(sharedPreferences.getString("verifier", null))
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
            Log.i("CharacterViewModel", "received authorization token")
            //TODO create new character w/ token
        }
    }
}