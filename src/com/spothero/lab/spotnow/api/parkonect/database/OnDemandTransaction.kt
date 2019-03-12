package com.spothero.lab.spotnow.api.parkonect.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Table

/**
 * On demand transaction with entry and exit details and its statuses.
 */
class OnDemandTransaction(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OnDemandTransaction>(OnDemandTransactions)

    var transactionId by OnDemandTransactions.transactionId
    var amount by OnDemandTransactions.amount
    var entryRecord by OnDemandEvent via OnDemandEntryTrans
    var exitRecord by OnDemandEvent via OnDemandExitTrans
}

object OnDemandExitTrans : Table() {
    val transaction = reference("transaction", OnDemandTransactions).primaryKey(0)
    val event = reference("event", OnDemandEvents).primaryKey(1)
}

object OnDemandEntryTrans : Table() {
    val transaction = reference("transaction", OnDemandTransactions).primaryKey(0)
    val event = reference("event", OnDemandEvents).primaryKey(1)
}

object OnDemandTransactions : LongIdTable() {
    val transactionId = varchar("transaction_id", 100)
    val amount = float("amount").nullable()
}

/**
 * Call entry from API for tracking event of entry or exit via barcode
 */
class OnDemandEvent(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OnDemandEvent>(OnDemandEvents)

    var barcode by OnDemandEvents.barcode
    var barcodeHash by OnDemandEvents.barcodeHash
    var garageId by OnDemandEvents.garageId
    var laneId by OnDemandEvents.laneId
    var entryTime by OnDemandEvents.entryTime
    var response by OnDemandResponse referencedOn OnDemandEvents.response
}

object OnDemandEvents : LongIdTable() {
    val barcodeHash = long("bar_code_hash")
    val barcode = text("bar_code")
    val garageId = integer("garage_id").index()
    val laneId = integer("lane_id")
    val entryTime = datetime("entry_time").index()
    val response = reference("response_id", OnDemandResponses)
}

/**
 * Response reporting if was successful and any error message - for tracking...
 */
class OnDemandResponse(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OnDemandResponse>(OnDemandResponses)

    var success by OnDemandResponses.success
    var errorMessage by OnDemandResponses.errorMessage
    var responseTime by OnDemandResponses.responseTime
    var requestTime by OnDemandResponses.requestTime
}

object OnDemandResponses : LongIdTable() {
    val success = bool("success").nullable()
    val errorMessage = varchar("error_message", 250).nullable()
    val responseTime = datetime("response_time").nullable()
    val requestTime = datetime("request_time")
}