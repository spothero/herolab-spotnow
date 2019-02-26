package com.spothero.lab.api

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    routing {
        authenticate("apiAuth") {
            route("/api") {
                get {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}