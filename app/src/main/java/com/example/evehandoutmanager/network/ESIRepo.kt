package com.example.evehandoutmanager.network

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
const val SSO_BASEURL = "login.eveonline.com/v2/oauth"
const val ESI_BASEURL = "esi.evetech.net/latest"

object ESIRepo {
    fun getLoginIntent(clientID: String, redirect_uri: String, state: String, challenge: String): Intent {
        //create URI to pass to intent
        val builder = Uri.Builder()
        builder.scheme("https")
            .encodedAuthority("$SSO_BASEURL/authorize/")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirect_uri)
            .appendQueryParameter("client_id", clientID)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)

        return Intent(Intent.ACTION_VIEW, builder.build())
    }

    //Creates a pair of challance/verifier strings for login verification
    fun generateChallenge() : Pair<String, String>{
        //generate 32 bytes
        //base 64url encode them
        val random = Random(Calendar.getInstance().timeInMillis)
        val byteArray = random.nextBytes(32)
        val verifier = String(Base64.getUrlEncoder().encode(byteArray)).replace("=", "")

        //sha-256 verifier
        //64url encode hash output
        fun hash(verifier: String): ByteArray {
            val bytes = verifier.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            return md.digest(bytes)
        }
        val challenge = String(Base64.getUrlEncoder().encode(hash(verifier))).replace("=", "")
        return Pair(challenge, verifier)
    }

    suspend fun handleCallback(clientID: String, code: String, verifier: String) : Token {
        return withContext(Dispatchers.IO) {
            Log.i("ESIRepo", verifier)
            val token : Token = Sso.retrofitInterface.handleLoginCallback(
                clientID = clientID,
                code = code,
                verifier = verifier
            ).await()
            return@withContext token
        }
    }

}