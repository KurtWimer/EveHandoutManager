package com.example.evehandoutmanager.network

import com.auth0.android.jwt.JWT
import com.google.gson.annotations.SerializedName
import java.util.*

data class Token(
    @SerializedName("access_token"  ) var accessToken  : String,
    @SerializedName("expires_in"    ) var expiresIn    : Int,
    @SerializedName("token_type"    ) var tokenType    : String,
    @SerializedName("refresh_token" ) var refreshToken : String
){
    val charcterID : String
        get() = requireNotNull(JWT(this.accessToken).subject).split(":").last()
    //private val jwt = JWT(this.accessToken)

    fun validate(): Boolean {
        fun validateIssuer(): Boolean {
            return when (JWT(this.accessToken).issuer){
                "login.eveonline.com" -> true
                "https://login.eveonline.com" -> true
                else -> false
            }
        }
        fun validateAudience() : Boolean { return JWT(this.accessToken).audience?.get(0) == "EVE Online" }

        return validateIssuer() && validateAudience()
    }
}
