package com.spothero.lab.spotnow.api.parkonect.model

import com.spothero.lab.spotnow.api.parkonect.database.OnDemandEvent
import java.util.*

data class TransactionDetail(
    val id: String,
    val barCode: String,
    val amount: Float? = null,
    val entryEvent: TransactionEvent,
    val exitEvent: TransactionEvent? = null
)

data class TransactionEvent(
    val laneId: Int,
    val garageId: Int,
    val time: Date
) {
    companion object {
        fun fromEvent(event: OnDemandEvent): TransactionEvent {
            return TransactionEvent(event.laneId, event.garageId, event.entryTime.toDate())
        }
    }
}