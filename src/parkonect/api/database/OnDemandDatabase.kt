package com.spothero.lab.parkonect.api.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import parkonect.api.model.OnDemandEntryRequest
import parkonect.api.model.OnDemandExitRequest
import java.sql.Connection
import java.util.*


class OnDemandDatabase(dbConnection: String = "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1") {

    companion object {
        val log = KotlinLogging.logger(OnDemandDatabase::class.java.simpleName)
    }

    private val db: Database

    init {
        db = Database.connect(dbConnection, pickDriver(dbConnection))
        serializableTransaction {
            SchemaUtils.createMissingTablesAndColumns(OnDemandTransactions)
            SchemaUtils.createMissingTablesAndColumns(OnDemandEvents)
            SchemaUtils.createMissingTablesAndColumns(OnDemandResponses)
            SchemaUtils.createMissingTablesAndColumns(OnDemandExitTrans)
            SchemaUtils.createMissingTablesAndColumns(OnDemandEntryTrans)
        }
    }

    private fun pickDriver(dbConnJdbc: String): String {
        if (dbConnJdbc.startsWith("jdbc:postgresql")) {
            return "org.postgresql.Driver"
        } else { //default
            return "org.h2.Driver"
        }
    }


    fun recordEntry(request: OnDemandEntryRequest, requestTime: DateTime = DateTime.now()): String? =
        serializableTransaction {
            var barHash = request.barcode.computeHash()

            var entryEvent = OnDemandEvent.new {
                barcodeHash = request.barcode.computeHash()
                barcode = request.barcode
                entryTime = DateTime(request.entryTime)
                garageId = request.garageId
                laneId = request.laneId
                response = OnDemandResponse.new {
                    success = false
                    errorMessage = "Not completed"
                    this.requestTime = requestTime
                }
            }

            var activeTransactions =
                OnDemandTransactions.innerJoin(OnDemandEntryTrans).innerJoin(OnDemandEvents)
                    .slice(OnDemandTransactions.columns)
                    .select { OnDemandEvents.barcodeHash eq barHash }
                    .distinct()
                    .map { OnDemandTransaction.wrapRow(it) }
            var withExitTransactions = OnDemandTransactions.innerJoin(OnDemandExitTrans).innerJoin(OnDemandEvents)
                .slice(OnDemandTransactions.columns)
                .select { OnDemandEvents.barcodeHash eq barHash }
                .distinct()
                .map { OnDemandTransaction.wrapRow(it) }

            activeTransactions.toMutableList()
                .removeIf { activeTrans -> withExitTransactions.firstOrNull { itExit -> itExit.transactionId == activeTrans.transactionId } != null }

            val responseMessage = if (activeTransactions.isNotEmpty()) {
                "There is already Entry transaction active for this barcode."
            } else {
                OnDemandTransaction.new {
                    transactionId = UUID.randomUUID().toString()
                    amount = null
                }.apply {
                    entryRecord = SizedCollection(listOf(entryEvent))
                }
                null
            }
            entryEvent.response.apply {
                success = responseMessage == null
                errorMessage = responseMessage
                responseTime = DateTime.now()
            }
            return@serializableTransaction responseMessage
        }

    fun recordExit(request: OnDemandExitRequest, requestTime: DateTime = DateTime.now()): Pair<String, String?> =
        serializableTransaction {
            var barHash = request.barcode.computeHash()
            var exitEvent = OnDemandEvent.new {
                barcodeHash = barHash
                barcode = request.barcode
                entryTime = DateTime(request.entryTime)
                garageId = request.garageId
                laneId = request.laneId
                response = OnDemandResponse.new {
                    success = false
                    errorMessage = "Not completed"
                    this.requestTime = requestTime
                }
            }
            var withExitTransactions = OnDemandTransactions.innerJoin(OnDemandExitTrans).innerJoin(OnDemandEvents)
                .slice(OnDemandTransactions.columns)
                .select { OnDemandEvents.barcodeHash eq barHash }
                .distinct()
                .map { OnDemandTransaction.wrapRow(it).transactionId }
                .toList()

            var activeTransactions =
                OnDemandTransactions.innerJoin(OnDemandEntryTrans).innerJoin(OnDemandEvents)
                    .slice(OnDemandTransactions.columns)
                    .select {
                        OnDemandEvents.barcodeHash eq barHash and OnDemandTransactions.transactionId.notInList(
                            withExitTransactions
                        )
                    }
                    .distinct()
                    .map { OnDemandTransaction.wrapRow(it) }

            var transId: String = ""
            var responseMessage: String? = if (activeTransactions.size > 1) {
                // this should not happen
                log.warn { "There is ${activeTransactions.size} active transactions for same hashCode" }
                "Multiple Entries found for this barcode"
            } else {
                val activeTransaction = activeTransactions.firstOrNull()
                if (activeTransaction != null) {
                    transId = activeTransaction.transactionId
                    activeTransaction.amount = request.amount
                    activeTransaction.exitRecord = SizedCollection(listOf(exitEvent))
                    null
                } else {
                    transId = withExitTransactions.first()
                    "Active Entry transaction not found for this BarCode"
                }
            }
            exitEvent.response.apply {
                success = responseMessage == null
                errorMessage = responseMessage
                responseTime = DateTime.now()
            }
            return@serializableTransaction Pair(transId, responseMessage)
        }

    private fun <T> serializableTransaction(function: Transaction.() -> T): T {
        return transaction(Connection.TRANSACTION_SERIALIZABLE, 3, db, function)
    }

}

fun String.computeHash(): Long {
    var h = 1125899906842597L // prime
    val len = length
    for (i in 0 until len) {
        h = 31 * h + this[i].toByte()
    }
    return h
}

