package com.example.evehandoutmanager


import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.evehandoutmanager.database.HandoutDao
import com.example.evehandoutmanager.database.LocalDatabase
import com.example.evehandoutmanager.home.Handout
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import java.io.IOException
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class DBTests {
    @Rule @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var handoutDao: HandoutDao
    private lateinit var db: LocalDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, LocalDatabase::class.java).build()
        handoutDao = db.handoutDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadList() {
        val handout1 = createHandout(1, 1)
        val handout2 = createHandout(2, 1)
        val handout3 = createHandout(3, 2)
        val handoutList = listOf(handout1, handout2, handout3)
        handoutDao.insert(*handoutList.toTypedArray())
        val all = handoutDao.getHandouts().blockingObserve()
        val byID = handoutDao.getPlayersHandouts(1)

        assertThat(all, equalTo(handoutList))
        assertThat(byID, equalTo(handoutList.dropLast(1)))
    }

    @Test
    fun writeAndReadMostRecent() {
        val handout1 = createHandout(1, 1)
        val handout2 = createHandout(2, 1)
        val handout3 = createHandout(3, 2)
        val handoutList = listOf(handout1, handout2, handout3)
        handoutDao.insert(*handoutList.toTypedArray())
        val mostRecent = handoutDao.getMostRecentHandout()

        assertThat(mostRecent, equalTo(handout3))
    }

    @Test
    fun writeAndDelete() {
        val handout1 = createHandout(1, 1)
        val handout2 = createHandout(2, 2)
        val handout3 = createHandout(3, 2)
        val handoutList = listOf(handout1, handout2, handout3)
        handoutDao.insert(*handoutList.toTypedArray())
        handoutDao.delete(handout1)
        val afterDelete = handoutDao.getHandouts().blockingObserve()
        handoutDao.deleteAll()
        val afterDeleteAll = handoutDao.getHandouts().blockingObserve()

        assertThat(afterDelete, equalTo(listOf(handout2, handout3)))
        assertThat(afterDeleteAll, equalTo(emptyList<Handout>()))
    }
}

