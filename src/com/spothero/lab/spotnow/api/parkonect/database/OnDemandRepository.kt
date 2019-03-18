package com.spothero.lab.spotnow.api.parkonect.database

import com.spothero.lab.spotnow.api.parkonect.model.OnDemandEntryRequest
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandExitRequest
import com.spothero.lab.spotnow.api.parkonect.model.TransactionDetail
import com.spothero.lab.spotnow.api.parkonect.model.TransactionEvent
import com.spothero.lab.spotnow.database.BaseRepository
import mu.KLogger
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import java.util.*


class OnDemandRepository(database: Database) : BaseRepository(database) {

    companion object {
        private val logStatic = KotlinLogging.logger(OnDemandRepository::class.java.simpleName)
    }

    override val log: KLogger
        get() = logStatic

    override fun initDb() {
        runTransaction {

            SchemaUtils.createMissingTablesAndColumns(OnDemandTransactions)
            SchemaUtils.createMissingTablesAndColumns(OnDemandEvents)
            SchemaUtils.createMissingTablesAndColumns(OnDemandResponses)
            SchemaUtils.createMissingTablesAndColumns(OnDemandExitTrans)
            SchemaUtils.createMissingTablesAndColumns(OnDemandEntryTrans)
        }
    }

    fun recordEntry(request: OnDemandEntryRequest, requestTime: DateTime = DateTime.now()): String? =
        runTransaction {
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
            return@runTransaction responseMessage
        }

    fun recordExit(request: OnDemandExitRequest, requestTime: DateTime = DateTime.now()): Pair<String, String?> =
        runTransaction {
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
            return@runTransaction Pair(transId, responseMessage)
        }

    fun topNTransactions(limit: Int): List<TransactionDetail> = runTransaction {
        return@runTransaction OnDemandTransactions.innerJoin(OnDemandEntryTrans).innerJoin(OnDemandEvents).selectAll()
            .orderBy(OnDemandEvents.entryTime, isAsc = false)
            .limit(limit)
            .distinct()
            .asSequence()
            .map { OnDemandTransaction.wrapRow(it) }
            .map { trans ->
                TransactionDetail(
                    trans.transactionId, trans.entryRecord.first().barcode, trans.amount,
                    trans.entryRecord.first().let { TransactionEvent.fromEvent(it) },
                    trans.exitRecord.firstOrNull()?.let { TransactionEvent.fromEvent(it) }
                )
            }.toList()
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

