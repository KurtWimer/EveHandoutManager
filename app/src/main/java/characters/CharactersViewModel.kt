package characters

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CharactersViewModel (private val application: Application){
    //private val dataSource TODO

    //currently logged in Characters
    private val _characterList = MutableLiveData<List<Character>>()
    val characterList: LiveData<List<Character>>
        get() = _characterList

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