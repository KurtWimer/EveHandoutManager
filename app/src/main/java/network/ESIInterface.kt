package network


import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ESIInterface {

    //Takes a clientID, authorization code, and verifier and returns a new access token
    @FormUrlEncoded
    @POST("/token")
    fun handleLoginCallback(
        @Field("client_id") clientID: String,
        @Field("code") code: String,
        @Field("code_verifier") verifier: String,
        @Field("authority") authority: String = "login.eveonline.com/v2/oauth/token",
        @Field("grant_type") grant: String = "authorization_code") : Call<Token>

}