package com.spothero.lab

import com.spothero.lab.parkonect.api.database.OnDemandDatabase
import org.junit.Before
import org.junit.Test
import parkonect.api.model.OnDemandEntryRequest
import parkonect.api.model.OnDemandExitRequest
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DatabaseTest {
    lateinit var db: OnDemandDatabase
    @Before
    fun setup() {
        db = OnDemandDatabase("jdbc:h2:mem:db${System.currentTimeMillis()};DB_CLOSE_DELAY=-1")
    }

    @Test
    fun testEntryExit() {
        var respEntry = db.recordEntry(OnDemandEntryRequest(10, "testBAR", 10))
        assertNull(respEntry)
        var respExit = db.recordExit(OnDemandExitRequest(10, "testBAR", 10, 19f))
        println(respExit)
        assertNotNull(respExit.first)
        assertNotNull(UUID.fromString(respExit.first))
    }

    @Test
    fun testDoubleEntry() {
        var respEntry = db.recordEntry(OnDemandEntryRequest(10, "testBAR", 10))
        assertNull(respEntry)
        respEntry = db.recordEntry(OnDemandEntryRequest(10, "testBAR", 10))
        println(respEntry)
        assertNotNull(respEntry)
    }

    @Test
    fun testDoubleExit() {
        var respEntry = db.recordEntry(OnDemandEntryRequest(10, "testBAR", 10))
        assertNull(respEntry)
        var respExit = db.recordExit(OnDemandExitRequest(10, "testBAR", 10, 19f))
        assertNull(respExit.second)
        assertNotNull(respExit.first)
        assertNotNull(UUID.fromString(respExit.first))
        respExit = db.recordExit(OnDemandExitRequest(10, "testBAR", 10, 19f))
        println(respExit)
        assertNotNull(respExit.second)
        assertNotNull(UUID.fromString(respExit.first))
    }
}