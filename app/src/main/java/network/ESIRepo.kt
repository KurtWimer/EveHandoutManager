package network

import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random
import retrofit2.await

const val SCOPES = "esi-wallet.read_character_wallet.v1 esi-contracts.read_character_contracts.v1"
const val BASEURL = "login.eveonline.com/v2/oauth"

//TODO can this be refactored into only support methods?
class ESIRepo {
    //variables used for communication with server
    private lateinit var verifier: String
    private lateinit var challenge : String
    init {
        generateChallenge()
    }
    fun getLoginIntent(clientID: String, redirect_uri: String, state: String): Intent {
        //create URI to pass to intent
        val builder = Uri.Builder()
        builder.scheme("https")
            .encodedAuthority("$BASEURL/authorize/")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirect_uri)
            .appendQueryParameter("client_id", clientID)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)

        return Intent(Intent.ACTION_VIEW, builder.build())
    }

    private fun generateChallenge(){
        //generate 32 bytes
        //base 64url encode them
        val random = Random(15)//TODO use a variable seed
        val byteArray = random.nextBytes(32)
        verifier = String(Base64.getUrlEncoder().encode(byteArray))

        //sha-256 verifier
        //64url encode hash output
        fun hash(verifier: String): ByteArray {
            val bytes = verifier.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            return md.digest(bytes)
        }
        challenge = String(Base64.getUrlEncoder().encode(hash(verifier)))
    }

    suspend fun handleCallback(clientID: String, code: String) {
        withContext(Dispatchers.IO) {
            Log.i("ESIRepo", verifier)
            val token = Network.esi.handleLoginCallback(
                clientID = clientID,
                code = code,
                verifier = verifier
            ).await()
            return@withContext token
        }
    }

}