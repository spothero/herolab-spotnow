package com.spothero.lab.parkonect.api

import com.spothero.lab.parkonect.api.database.OnDemandDatabase
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
import parkonect.api.model.OnDemandEntryRequest
import parkonect.api.model.OnDemandEntryResponse
import parkonect.api.model.OnDemandExitRequest
import parkonect.api.model.OnDemandExitResponse


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

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
                    var errorMessage = OnDemandDatabase().recordEntry(entryRequest)

                    val respBody = OnDemandEntryResponse(errorMessage)
                    call.respond(respBody)
                }
                post("exit") {
                    val reqBody = call.receive<OnDemandExitRequest>()
                    log.info("Parsed body: $reqBody")
                    var respPair = OnDemandDatabase().recordExit(reqBody)
                    var respBody = if (respPair.second == null) {
                        OnDemandExitResponse(true, respPair.first)
                    } else {
                        OnDemandExitResponse(false, null, respPair.second)
                    }
                    call.respond(respBody)
                }
            }
        }
    }
}
