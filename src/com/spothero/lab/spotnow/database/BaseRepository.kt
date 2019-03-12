package com.spothero.lab.spotnow.database

import mu.KLogger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

abstract class BaseRepository(private val db: Database) {

    companion object {
        fun h2DbMem(dbName: String = "testDb"): Pair<String, String> {
            return Pair("jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        }

        fun h2DbFile(filePath: String): Pair<String, String> {
            return Pair("jdbc:h2:file:$filePath;DB_CLOSE_DELAY=-1", "org.h2.Driver")
        }

        fun postgreSql(address: String, dbName: String): Pair<String, String> {
            return Pair("jdbc:postgresql://$address/$dbName", "org.postgresql.Driver")
        }

    }

    init {
        initDb()
    }

    abstract val log: KLogger

    /**
     * Run schema initialization
     */
    abstract fun initDb()


    fun <T> runTransaction(function: Transaction.() -> T): T {
        return transaction(Connection.TRANSACTION_SERIALIZABLE, 3, db, function)
    }
}

