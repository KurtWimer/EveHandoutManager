package network

import com.auth0.android.jwt.JWT
import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("access_token"  ) var accessToken  : String,
    @SerializedName("expires_in"    ) var expiresIn    : Int,
    @SerializedName("token_type"    ) var tokenType    : String,
    @SerializedName("refresh_token" ) var refreshToken : String
){
    fun getCharacterID() : String {
        val jwt = JWT(this.accessToken)
        val sub : String = requireNotNull(jwt.subject)
        return sub.split(":").last()
    }
}
