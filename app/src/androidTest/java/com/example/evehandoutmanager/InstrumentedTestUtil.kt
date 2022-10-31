package com.example.evehandoutmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.evehandoutmanager.home.Handout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


fun createHandout(handoutID: Long, receiverID: Int): Handout {
        return Handout (
            id = handoutID,
            shipName = "Test",
            receiverName= "Test",
            receiverID= receiverID,
            receiverIconUrl= "www.test.com")
}

fun <T> LiveData<T>.blockingObserve(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)

    val observer = Observer<T> { t ->
        value = t
        latch.countDown()
    }

    observeForever(observer)

    latch.await(2, TimeUnit.SECONDS)
    return value
}

