package com.spothero.lab

import com.spothero.lab.spotnow.api.parkonect.database.OnDemandRepository
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandEntryRequest
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandExitRequest
import com.spothero.lab.spotnow.database.BaseRepository
import com.spothero.lab.spotnow.di.databaseModule
import com.spothero.lab.spotnow.di.repositoryModule
import com.spothero.lab.spotnow.di.serviceModule
import org.jetbrains.exposed.sql.Database
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext
import org.koin.standalone.get
import org.koin.test.AutoCloseKoinTest
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DatabaseTest : AutoCloseKoinTest() {

    lateinit var db: OnDemandRepository
    @Before
    fun startup() {
        databaseModule = module {
            single {
                val (dbUrl, driver) = BaseRepository.h2DbMem("name:${DateTime.now().millis}")
                Database.connect(dbUrl, driver)// create db connection before using in DI
            }
        }
        StandAloneContext.startKoin(listOf(databaseModule, repositoryModule, serviceModule))
        db = get()
    }

    @Test
    fun testEntryExit() {
        var respEntry = db.recordEntry(
            OnDemandEntryRequest(
                10,
                "testBAR",
                10
            )
        )
        assertNull(respEntry)
        var respExit = db.recordExit(
            OnDemandExitRequest(
                10,
                "testBAR",
                10,
                19f
            )
        )
        println(respExit)
        assertNotNull(respExit.first)
        assertNotNull(UUID.fromString(respExit.first))
    }

    @Test
    fun testDoubleEntry() {
        var respEntry = db.recordEntry(
            OnDemandEntryRequest(
                10,
                "testBAR",
                10
            )
        )
        assertNull(respEntry)
        respEntry = db.recordEntry(OnDemandEntryRequest(10, "testBAR", 10))
        println(respEntry)
        assertNotNull(respEntry)
    }

    @Test
    fun testDoubleExit() {
        var respEntry = db.recordEntry(
            OnDemandEntryRequest(
                10,
                "testBAR",
                10
            )
        )
        assertNull(respEntry)
        var respExit = db.recordExit(
            OnDemandExitRequest(
                10,
                "testBAR",
                10,
                19f
            )
        )
        assertNull(respExit.second)
        assertNotNull(respExit.first)
        assertNotNull(UUID.fromString(respExit.first))
        respExit = db.recordExit(
            OnDemandExitRequest(
                10,
                "testBAR",
                10,
                19f
            )
        )
        println(respExit)
        assertNotNull(respExit.second)
        assertNotNull(UUID.fromString(respExit.first))
    }
}