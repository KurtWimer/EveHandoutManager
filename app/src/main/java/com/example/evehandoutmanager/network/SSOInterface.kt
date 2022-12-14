package com.example.evehandoutmanager.network


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val DATASOURCE = "tranquility"

interface SSOInterface {
    //Takes a clientID, authorization code, and verifier and returns a new access token
    @FormUrlEncoded
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Host: login.eveonline.com"
    )
    @POST("token")
    fun handleLoginCallback(
        @Field("client_id") clientID: String,
        @Field("code") code: String,
        @Field("code_verifier") verifier: String,
        @Field("authority") authority: String = "https://login.eveonline.com/v2/oauth/token",
        @Field("grant_type") grant: String = "authorization_code") : Call<Token>

    @FormUrlEncoded
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Host: login.eveonline.com"
    )

    @POST("token")
    fun refreshAccessToken(
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientID: String,
        @Field("grant_type") grantType: String = "refresh_token",
    ) : Call<Token>
}

object Sso {
    private val client =  OkHttpClient.Builder()
        //.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
        //.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://$SSO_BASEURL/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val retrofitInterface: SSOInterface = retrofit.create(SSOInterface::class.java)
}