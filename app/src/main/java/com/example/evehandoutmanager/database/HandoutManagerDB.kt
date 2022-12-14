package com.example.evehandoutmanager.database

import com.example.evehandoutmanager.accounts.Account
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.evehandoutmanager.fleetConfiguration.FleetConfigItem
import com.example.evehandoutmanager.home.Handout

@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    fun getAccounts(): LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account : Account)

    @Update(entity = Account::class)
    fun update(account: Account)

    @Delete
    fun delete(account: Account)
}

@Dao
interface HandoutDao {
    @Query("SELECT * FROM handout")
    fun getHandouts() : LiveData<List<Handout>>

    @Query ("SELECT * FROM handout WHERE receiverID = :id")
    fun getPlayersHandouts(id : Int) : List<Handout>

    @Query("SELECT * FROM handout ORDER BY id DESC LIMIT 1")
    fun getMostRecentHandout(): Handout

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg handouts: Handout)
    
    @Delete
    fun delete(entry : Handout)

    @Query("DELETE FROM handout")
    fun deleteAll()
}

@Dao
interface FleetDao {
    @Query("SELECT * FROM FleetConfigItem")
    fun getConfigLive() : LiveData<List<FleetConfigItem>>

    @Query("SELECT * FROM FleetConfigItem")
    fun getConfig() : List<FleetConfigItem>

    @Query("SELECT iskValue FROM FleetConfigItem")
    fun getKeys() : List<Int>

    @Insert
    fun insert(vararg configs: FleetConfigItem)

    @Delete
    fun delete(entry: FleetConfigItem)

    @Query("DELETE FROM FleetConfigItem")
    fun deleteAll()
}

@Database(entities = [Account::class, Handout::class, FleetConfigItem::class], version = 4)
abstract class LocalDatabase : RoomDatabase() {
    abstract val accountDao : AccountDao
    abstract val handoutDao : HandoutDao
    abstract val fleetDao : FleetDao
}

private lateinit var INSTANCE: LocalDatabase

fun getDatabase(context: Context): LocalDatabase {
    synchronized(LocalDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                LocalDatabase::class.java,
                "HandoutManagerDB").fallbackToDestructiveMigration().build()
        }
    }
    return INSTANCE
}