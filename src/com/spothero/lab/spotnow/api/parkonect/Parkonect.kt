package com.spothero.lab.spotnow.api.parkonect

import com.spothero.lab.spotnow.api.parkonect.model.OnDemandEntryRequest
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandEntryResponse
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandExitRequest
import com.spothero.lab.spotnow.api.parkonect.model.OnDemandExitResponse
import com.spothero.lab.spotnow.api.parkonect.service.OnDemandService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import org.koin.ktor.ext.inject


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val onDemandService: OnDemandService by inject()

    routing {

        authenticate("apiAuth") {
            route("/api/parkonect") {
                get {
                    call.respond(HttpStatusCode.OK)
                }
                post("entry") {
                    log.info(call.request.headers.toString())
                    var entryRequest = call.receive<OnDemandEntryRequest>()
                    log.info("Received $entryRequest")
                    var errorMessage = onDemandService.recordEntry(entryRequest)

                    val respBody = OnDemandEntryResponse(errorMessage)
                    call.respond(respBody)
                }
                post("exit") {
                    val reqBody = call.receive<OnDemandExitRequest>()
                    log.info("Parsed body: $reqBody")
                    var respPair = onDemandService.recordExit(reqBody)
                    var respBody = if (respPair.second == null) {
                        OnDemandExitResponse(true, respPair.first)
                    } else {
                        OnDemandExitResponse(false, null, respPair.second)
                    }
                    call.respond(respBody)
                }
            }
        }

        route("/api/transactions") {
            get {
                // list last 20 transactions
                val limit: Int = call.parameters["limit"]?.toIntOrNull() ?: 20
                val transList = onDemandService.listTransactions(limit)
                call.respond(transList)// todo json response per route?
            }
        }

    }
}
