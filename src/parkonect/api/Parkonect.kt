package com.spothero.lab.parkonect.api

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
import java.util.*


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

                    val respBody = OnDemandEntryResponse(true)
                    call.respond(respBody)
                }
                post("exit") {
                    val reqBody = call.receive<OnDemandExitRequest>()
                    log.info("Parsed body: $reqBody")
                    val respBody = OnDemandExitResponse(true, Random().nextLong())
                    call.respond(respBody)
                }
            }
        }
    }
}
