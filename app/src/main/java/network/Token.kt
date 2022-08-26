package network

import com.auth0.android.jwt.JWT
import com.google.gson.annotations.SerializedName
import java.util.*

data class Token(
    @SerializedName("access_token"  ) var accessToken  : String,
    @SerializedName("expires_in"    ) var expiresIn    : Int,
    @SerializedName("token_type"    ) var tokenType    : String,
    @SerializedName("refresh_token" ) var refreshToken : String
){

    //private val jwt = JWT(this.accessToken)

    fun getCharacterID() : String {
        return requireNotNull(JWT(this.accessToken).subject).split(":").last()
    }

    fun getExpiration(): Date {
        return requireNotNull(JWT(this.accessToken).expiresAt)
    }

    fun isExpired(): Boolean {
        return requireNotNull(JWT(this.accessToken).isExpired(0))
    }

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
