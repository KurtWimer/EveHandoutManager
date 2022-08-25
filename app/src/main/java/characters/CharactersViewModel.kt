package characters

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.example.evehandoutmanager.R
import kotlinx.coroutines.launch
import network.ESIRepo

class CharactersViewModel (
    private val app: Application) : AndroidViewModel(app){
    private val repo = ESIRepo() //TODO dagger
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
        val intent = repo.getLoginIntent(
            app.getString(R.string.client_id),
            app.getString(R.string.redirect_uri),
            app.getString(R.string.state)
        )
        _navigateToSSO.value = intent
    }
    //called after navigating
    fun onLoginButtonComplete(){ _navigateToSSO.value = null }

    fun handleCallback(code: String) {
        viewModelScope.launch {
            val clientID = app.getString(R.string.client_id)
            val token = repo.handleCallback(clientID, code)
            Log.i("CharacterViewModel", token.toString())
        }
    }
}


//Simple Factory for Character ViewModel
class CharactersViewModelFactory(
    //private val dataSource: SleepDatabaseDao, TODO
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharactersViewModel::class.java)) {
            return CharactersViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}