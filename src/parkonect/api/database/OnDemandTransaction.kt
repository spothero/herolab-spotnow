package com.spothero.lab.parkonect.api.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable

/**
 * On demand transaction with entry and exit details and its statuses.
 */
class OnDemandTransaction(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<OnDemandTransaction>(OnDemandTransactions)

    var transactionId by OnDemandTransactions.transactionId
    var amount by OnDemandTransactions.amount
    var entryRecord by OnDemandEvent referencedOn OnDemandTransactions.entryRecord
    var exitRecord by OnDemandEvent optionalReferencedOn OnDemandTransactions.exitRecord
}

object OnDemandTransactions : LongIdTable() {
    val transactionId = varchar("transaction_id", 100)
    val amount = float("amount").nullable()
    val entryRecord = reference("entry_id", OnDemandEvents)
    val exitRecord = reference("exit_id", OnDemandEvents).nullable()
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
    val laneId = integer("lane_id").nullable()
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
    val responseTime = datetime("response_time")
    val requestTime = datetime("request_time")
}