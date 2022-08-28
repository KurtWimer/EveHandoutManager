package network

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
    @GET("accounts/{id}/")
    fun getCharacter(
        @Path("id") characterID : String,
        @Query("datasource") datasource: String = DATASOURCE) : Call<CharacterResponse>

    @GET("accounts/{id}/portrait/")
    fun getPortrait(
        @Path("id") characterID : String,
        @Query("datasource") datasource: String = DATASOURCE) : Call<Portrait>
}

object Esi {
    private val client =  OkHttpClient.Builder()
        //.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        //TODO rate limit requests https://stackoverflow.com/questions/41309103/how-can-i-queue-up-and-delay-retrofit-requests-to-avoid-hitting-an-api-rate-limi
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://$ESI_BASEURL/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val retrofitInterface: ESIInterface = retrofit.create(ESIInterface::class.java)
}