package com.example.evehandoutmanager.network

import com.example.evehandoutmanager.home.WalletEntry
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ESIInterface {
    @Headers("accept: application/json", "Cache-Control: no-cache")
    @GET("characters/{id}/")
    fun getCharacter(
        @Path("id") characterID : String,
        @Query("datasource") datasource: String = DATASOURCE) : Call<CharacterResponse>

    @GET("characters/{id}/portrait/")
    fun getPortrait(
        @Path("id") characterID : String,
        @Query("datasource") datasource: String = DATASOURCE) : Call<Portrait>

    @GET("characters/{id}/wallet/journal")
    fun getWalletJournal(
        @Path("id") characterID: String,
        @Query("token") accessToken: String,
        //maximum entries per page is 2500 if for some reason more was needed a method for determining how many pages is necessary would be needed
        @Query("page") page: Int = 1
    ) : Call<List<WalletEntry>>
}

object Esi {
    private val client =  OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://$ESI_BASEURL/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val retrofitInterface: ESIInterface = retrofit.create(ESIInterface::class.java)
}