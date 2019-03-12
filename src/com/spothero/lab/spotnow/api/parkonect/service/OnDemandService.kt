package com.spothero.lab.spotnow.api.parkonect.service

import com.spothero.lab.parkonect.api.database.OnDemandRepository
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandEntryRequest
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandExitRequest
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
}