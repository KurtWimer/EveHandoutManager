package database

import accounts.Account
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {
    @Query("SELECT * FROM account") //TODO
    fun getAccounts(): LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account : Account)

    @Delete
    fun delete(account: Account)
}

@Database(entities = [Account::class], version = 1)
abstract class AccountDatabase : RoomDatabase() {
    abstract val accountDao : AccountDao
}

private lateinit var INSTANCE: AccountDatabase

fun getDatabase(context: Context): AccountDatabase {
        kotlin.synchronized(AccountDatabase::class.java) {
    if (!::INSTANCE.isInitialized) {
        INSTANCE = Room.databaseBuilder(context.applicationContext,
            AccountDatabase::class.java,
            "accounts").build()
    }
}
return INSTANCE
}