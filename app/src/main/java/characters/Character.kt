package characters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import network.Network

class Character (private val character_id : String){
    lateinit var name : String
    lateinit var iconUrl : String //TODO figure out what datatype icon is
}