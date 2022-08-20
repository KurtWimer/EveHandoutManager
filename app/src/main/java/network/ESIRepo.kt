package network

import android.content.Intent
import android.net.Uri
import com.example.evehandoutmanager.R
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

const val SCOPES = "esi-wallet.read_character_wallet.v1 esi-contracts.read_character_contracts.v1"
class ESIRepo {
    //variables used for communication with server
    private val state = "test"
    private lateinit var verifier: String
    private lateinit var challenge : String

    init {
        generateChallenge()
    }
    fun getLoginIntent(): Intent {
        //create URI to pass to intent
        val builder = Uri.Builder()
        builder.scheme("http")
            .authority("https://login.eveonline.com/v2/oauth/authorize/")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", R.string.redirect_uri.toString())
            .appendQueryParameter("client_id", R.string.client_id.toString())
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

    fun handleCallback(code: String, state: String){

    }

}