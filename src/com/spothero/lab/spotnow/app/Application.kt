package com.spothero.lab.spotnow.app

import com.github.ajalt.clikt.core.subcommands
import com.spothero.lab.spotnow.api.parkonect.ParkonectXmlConverter
import com.spothero.lab.spotnow.di.databaseModule
import com.spothero.lab.spotnow.di.repositoryModule
import com.spothero.lab.spotnow.di.serviceModule
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.event.Level

fun main(args: Array<String>): Unit {
    val onDemandEnabled = OnDemandCommmand().apply {
        this.subcommands(PostgreDbCommand(), H2DbCommand())
        this.main(args)
    }.let { it.enable != "false" }
    startKoin(listOf(databaseModule, repositoryModule, serviceModule))
    // todo figure out how to enable ktor module or disable
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(AutoHeadResponse)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Authentication) {
        basic("apiAuth") {
            realm = "SpotNow API Server"
            validate { if (it.name == "spothero" && it.password == "spothero") UserIdPrincipal(it.name) else null }
        }
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null }
        }
    }


    routing {
        install(ContentNegotiation) {
            jackson {
                // Configure Jackson's ObjectMapper here
                register(ContentType.Application.Xml, ParkonectXmlConverter()) {
                    // todo check for xml serialization per type
                }

            }
        }
        get("/") {
            call.respondText("HELLO WORLD! SpotNow!", contentType = ContentType.Text.Plain)
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

        authenticate("myBasicAuth") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }

    }
}

data class IndexData(val items: List<Int>)

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

