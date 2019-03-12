package com.spothero.lab.spotnow.api.parkonect.service

import com.spothero.lab.spotnow.api.parkonect.database.OnDemandRepository
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandEntryRequest
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandExitRequest
import com.spothero.lab.spotnow.api.parkonect.model.TransactionDetail
import org.joda.time.DateTime
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


class OnDemandService : KoinComponent {
    val repository: OnDemandRepository by inject()


    fun recordEntry(request: OnDemandEntryRequest, requestTime: DateTime = DateTime.now()): String? {
        return repository.recordEntry(request, requestTime)
    }

    fun recordExit(request: OnDemandExitRequest, requestTime: DateTime = DateTime.now()): Pair<String, String?> {
        return repository.recordExit(request, requestTime)
    }

    fun listTransactions(limit: Int = 20): List<TransactionDetail> {
        return repository.topNTransactions(limit)
    }
}