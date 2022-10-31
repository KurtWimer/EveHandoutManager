package com.example.evehandoutmanager

import com.example.evehandoutmanager.home.WalletEntry
import com.example.evehandoutmanager.network.ESIInterface
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory

class ESIInterfaceTests {
    private val client =  OkHttpClient.Builder().build()
    private lateinit var retrofit : Retrofit
    private lateinit var mockInterface: ESIInterface


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getWalletJournalResponseTest(){
        //initialize mock Server
        val mockServer = MockWebServer()
        mockServer.enqueue(MockResponse().setBody(sampleWalletResponseJSON))
        mockServer.start()
        val baseUrl = mockServer.url("/test/")
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        mockInterface = retrofit.create(ESIInterface::class.java)
        //expected response data
        val sampleWalletEntry = WalletEntry(
            amount = -17100.0,
            balance = 5203229322.3015,
            date = "2022-10-23T04:00:23Z",
            description = "Fee for activating a Upwell Jumpgate paid from Kurtisthe1st to Brave Pos Boys",
            firstPartyId = 2117457673,
            id = 20882462427L,
            reason = "",
            refType = "structure_gate_jump",
            secondPartyId = 98444656
        )

        runTest {
            val response = mockInterface.getWalletJournal("FakeID","FakeToken").await()
            assertThat(response.size, equalTo(71))
            assertThat(response[0], equalTo(sampleWalletEntry))
        }

        mockServer.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getWalletJournalPostTest(){
        //initialize mock Server
        val mockServer = MockWebServer()
        mockServer.enqueue(MockResponse().setBody(sampleWalletResponseJSON))
        mockServer.start()
        val baseUrl = mockServer.url("/test/")
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        mockInterface = retrofit.create(ESIInterface::class.java)

        runTest {
            mockInterface.getWalletJournal("FakeID","FakeToken").await()
        }

        val request = mockServer.takeRequest()
        assertThat(request.path, equalTo("/test/characters/FakeID/wallet/journal?token=FakeToken&page=1"))

        mockServer.close()
    }
}