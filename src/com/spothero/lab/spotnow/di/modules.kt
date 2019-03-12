package com.spothero.lab.spotnow.di

import com.spothero.lab.spotnow.api.parkonect.database.OnDemandRepository
import com.spothero.lab.spotnow.api.parkonect.service.OnDemandService
import org.koin.dsl.module.Module
import org.koin.dsl.module.module

lateinit var databaseModule: Module

val repositoryModule = module {
    single { OnDemandRepository(get()) }
}

val serviceModule = module {
    single { OnDemandService() }
}