package com.example.evehandoutmanager

import com.example.evehandoutmanager.accounts.Account
import com.example.evehandoutmanager.database.FleetDao
import com.example.evehandoutmanager.database.HandoutDao
import com.example.evehandoutmanager.database.LocalDatabase
import com.example.evehandoutmanager.home.Handout
import com.example.evehandoutmanager.home.processNewTrades
import com.example.evehandoutmanager.network.CharacterResponse
import com.example.evehandoutmanager.network.ESIInterface
import com.example.evehandoutmanager.network.Portrait
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.mock.Calls
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class HandoutProcessingTests {

    @Mock
    lateinit var fleetDao: FleetDao
    @Mock
    lateinit var handoutDao: HandoutDao
    @Mock
    lateinit var db : LocalDatabase
    @Mock
    lateinit var esi : ESIInterface
    private val account = Account(
        name = "TestAccount",
        iconURL = "www.test.com",
        characterID = 1,
        accessToken = "accessToken",
        _refreshToken =  "refreshToken"
    )
    @Captor
    private lateinit var handoutCaptor: ArgumentCaptor<Handout>

    @Before
    fun initMocks(){
        MockitoAnnotations.openMocks(this)
        `when`(db.fleetDao).thenReturn(fleetDao)
        `when`(db.handoutDao).thenReturn(handoutDao)
        `when`(handoutDao.getPlayersHandouts(anyInt())).thenReturn(listOf(createHandout(1L, 1)))
        `when`(fleetDao.getKeys()).thenReturn(listOf(1,2,3,4,5))
        `when`(esi.getCharacter(anyString(), anyString())).thenReturn(Calls.response(CharacterResponse(name = "test")))
        `when`(esi.getPortrait(anyString(), anyString())).thenReturn(Calls.response(Portrait(px128x128 = "test")))
    }

    @Test
    fun addSingleHandout() = runTest{
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleLoans.subList(0,1)))

        processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(0L),
            mostRecentTradeID = 0,
            esiInterface = esi
        )
        verify(handoutDao, times(1)).insert(any(Handout::class.java))
        verify(handoutDao, never()).delete(any(Handout::class.java))
    }


    @Test
    fun removeSingleHandout() = runTest{
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleReturns.subList(0,1)))

        processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(0L),
            mostRecentTradeID = 0,
            esiInterface = esi
        )
        verify(handoutDao, never()).insert(any(Handout::class.java))
        verify(handoutDao, times(1)).delete(any(Handout::class.java))
    }

    @Test
    fun addAndRemoveSingleHandout() = runTest{
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleReturns.subList(0,1) + sampleLoans.subList(0,1)))

        processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(0L),
            mostRecentTradeID = 0,
            esiInterface = esi
        )
        verify(handoutDao, times(1)).insert(any(Handout::class.java))
        verify(handoutDao, times(1)).delete(any(Handout::class.java))
    }

    @Test
    fun processOldData() = runTest{
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleReturns.subList(0,1) + sampleLoans.subList(0,1)))

        processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(),
            mostRecentTradeID = 0,
            esiInterface = esi
        )
        verify(handoutDao, never()).insert(any(Handout::class.java))
        verify(handoutDao, never()).delete(any(Handout::class.java))
    }

    @Test
    fun processTradesWithInvalidKey() = runTest{
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleLoans.subList(4,5)))

        processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(0L),
            mostRecentTradeID = 0,
            esiInterface = esi
        )
        verify(handoutDao, never()).insert(any(Handout::class.java))
        verify(handoutDao, never()).delete(any(Handout::class.java))
    }

    @Test
    fun processMultipleHandoutsAndReturns() = runTest {
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleLoans + sampleReturns))
        //Must mock Call object multiple times to prevent error from already returning response
        `when`(esi.getCharacter(anyString(), anyString()))
            .thenReturn(Calls.response(CharacterResponse(name = "test")))
            .thenReturn(Calls.response(CharacterResponse(name = "test")))
            .thenReturn(Calls.response(CharacterResponse(name = "test")))
            .thenReturn(Calls.response(CharacterResponse(name = "test")))
        `when`(esi.getPortrait(anyString(), anyString())).thenReturn(Calls.response(Portrait(px128x128 = "test")))
            .thenReturn(Calls.response(Portrait(px128x128 = "test")))
            .thenReturn(Calls.response(Portrait(px128x128 = "test")))
            .thenReturn(Calls.response(Portrait(px128x128 = "test")))
            .thenReturn(Calls.response(Portrait(px128x128 = "test")))

        processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(0L),
            mostRecentTradeID = 0,
            esiInterface = esi
        )

        verify(handoutDao, times(1)).insert(capture(handoutCaptor))
        //5th sample handout has invalid key so only 4 loans should be seen
        assertThat(handoutCaptor.allValues.size, equalTo(4))
        //Since dao is mocked it is possible to delete more than were inserted
        verify(handoutDao, times(5)).delete(any(Handout::class.java))
    }

    @Test
    fun returnValueIsCorrect() = runTest{
        `when`(esi.getWalletJournal(account.characterID.toString(), account.accessToken))
            .thenReturn(Calls.response(sampleReturns))

        val returnVal = processNewTrades(
            database = db,
            account = account,
            fleetStartTime = Date(0L),
            mostRecentTradeID = 0,
            esiInterface = esi
        )
        assertThat(returnVal, equalTo(sampleReturns.first().id))
    }
}