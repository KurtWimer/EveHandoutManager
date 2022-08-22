package characters

import android.app.Application
import androidx.lifecycle.*

class CharactersViewModel (private val app: Application) : AndroidViewModel(app){
    private val _navigateToSSO = MutableLiveData<Boolean>(false)
    val navigateToSSO : LiveData<Boolean>
        get() = _navigateToSSO

    //private val dataSource TODO

    //currently logged in Characters
    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>>
        get() = _characterList


    fun onLoginButton(){ _navigateToSSO.value = true }

    //called after navigating
    fun onLoginButtonComplete(){ _navigateToSSO.value = false }
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