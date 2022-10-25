package com.example.evehandoutmanager

import com.example.evehandoutmanager.home.Handout
import com.example.evehandoutmanager.home.WalletEntry
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

//Needed to use mockito matcher with Kotlin types
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

fun createHandout(handoutID: Long, receiverID: Int): Handout {
    return Handout (
        id = handoutID,
        shipName = "Test",
        receiverName= "Test",
        receiverID= receiverID,
        receiverIconUrl= "www.test.com")
}

val sampleLoans = listOf(
    WalletEntry(
        amount = 1.0,
        date = "2022-10-23T04:00:23Z",
        firstPartyId = 2,
        id = 20871132331,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 2.0,
        date = "2022-10-23T04:00:23Z",
        firstPartyId = 3,
        id = 20871132332,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 3.0,
        date = "2022-10-23T04:00:23Z",
        firstPartyId = 4,
        id = 20871132333,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 4.0,
        date = "2022-10-23T04:00:23Z",
        firstPartyId = 5,
        id = 20871132334,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 6.0,
        date = "2022-10-23T04:00:23Z",
        firstPartyId = 6,
        id = 20871132335,
        refType = "player_trading",
        secondPartyId = 1),
)

val sampleReturns = listOf(
    WalletEntry(
        amount = 0.0,
        date = "2022-10-23T04:01:23Z",
        firstPartyId = 2,
        id = 20871132331,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 0.0,
        date = "2022-10-23T04:01:23Z",
        firstPartyId = 3,
        id = 20871132332,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 0.0,
        date = "2022-10-23T04:01:23Z",
        firstPartyId = 4,
        id = 20871132333,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 0.0,
        date = "2022-10-23T04:01:23Z",
        firstPartyId = 5,
        id = 20871132334,
        refType = "player_trading",
        secondPartyId = 1),
    WalletEntry(
        amount = 0.0,
        date = "2022-10-23T04:01:23Z",
        firstPartyId = 6,
        id = 20871132335,
        refType = "player_trading",
        secondPartyId = 1),
)