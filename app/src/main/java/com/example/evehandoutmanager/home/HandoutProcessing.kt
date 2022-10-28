package com.example.evehandoutmanager.home

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.evehandoutmanager.accounts.Account
import com.example.evehandoutmanager.database.FleetDao
import com.example.evehandoutmanager.database.HandoutDao
import com.example.evehandoutmanager.database.LocalDatabase
import com.example.evehandoutmanager.network.ESIInterface
import com.example.evehandoutmanager.network.Esi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import retrofit2.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

//Converts WalletEntries into Handout objects
private suspend fun getNewHandouts(trades : List<WalletEntry>, dao: FleetDao, EsiInterface : ESIInterface = Esi.retrofitInterface): List<Handout> {
    val newHandouts = mutableListOf<Handout>()
    for (trade in trades){
        val fleetConfig = dao.getConfig()
        var shipName = "Unknown"
        for (item in fleetConfig){
            if (item.iskValue.toDouble() == trade.amount) shipName = item.shipName
        }
        val receiverName = EsiInterface.getCharacter(trade.firstPartyId.toString()).await().name
        val receiverIconUrl = EsiInterface.getPortrait(characterID = trade.firstPartyId.toString()).await().px128x128
        newHandouts.add(Handout(trade.id, shipName, receiverName!!, trade.firstPartyId, receiverIconUrl!!))
    }
    return newHandouts.toImmutableList()
}

//Removes ship returns
//Assumes each character only gets one Handout, if the character gets multiple it will
//return the ships in the order they were handed out
private fun processReturns(trades: List<WalletEntry>, dao: HandoutDao){
    for (trade in trades){
        val potentialMatches = dao.getPlayersHandouts(trade.firstPartyId)
        when (potentialMatches.size){
            0 -> Log.d("HomeViewModel", "Unable to find matching trade: $trade")
            else -> dao.delete(potentialMatches.first())
        }
    }
}

//Check if update to wallet transactions is available
//If it is available resets the timer for next update
fun walletUpdateAvailable(lastFetch : Date?, app: Application) : Boolean {
    if (lastFetch == null) return true
    //ESI only allows wallet updates every 60 minutes
    val CACHE_DURATION = 60
    val currentDate: Date = Calendar.getInstance().time
    val timeDiffInMinutes = getDateDiff(lastFetch, currentDate)
    val timeUntilNextUpdate = CACHE_DURATION - timeDiffInMinutes
    if(timeUntilNextUpdate > 0){
        Toast.makeText(app.applicationContext, "Next update available in $timeUntilNextUpdate minutes", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

//Finds and Processes all character trades
//Returns the ID of the most recent trade processed
suspend fun processNewTrades(database: LocalDatabase, account : Account, fleetStartTime: Date, mostRecentTradeID: Long, esiInterface : ESIInterface = Esi.retrofitInterface) : Long {
    return withContext(Dispatchers.IO) {
        //Get and filter all wallet transaction to find ship handouts
        val journal = esiInterface.getWalletJournal(account.characterID.toString(), account.accessToken).await()
        val keys = database.fleetDao.getKeys().toMutableList()
        val trades = journal.filter {
                    it.refType == "player_trading" &&
                    it.id > mostRecentTradeID &&
                    !convertDate(it.date).before(fleetStartTime)}
        val newHandouts = getNewHandouts(
            trades = trades.filter { it.amount.toInt() in keys},
            dao = database.fleetDao,
            EsiInterface = esiInterface
        )
        //add all new handouts to DB
        if (newHandouts.isNotEmpty()){
            database.handoutDao.insert(*newHandouts.toTypedArray())
        }
        processReturns(
            trades = trades.filter { it.amount == 0.toDouble() },
            dao = database.handoutDao
        )
        //Return value relies on the fact that ESI returns in order from most recent to oldest and trade ID only increases
        return@withContext journal.first().id
    }
}

//shamelessly stolen from https://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
private fun getDateDiff(date1: Date, date2: Date, timeUnit: TimeUnit = TimeUnit.MINUTES): Long {
    val diffInMillies = date2.time - date1.time
    return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS)
}

//Converts From iso-8601 into Date object
@SuppressLint("SimpleDateFormat")
private fun convertDate(date: String): Date {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    return format.parse(date)!!
}