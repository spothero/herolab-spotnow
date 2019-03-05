package com.spothero.lab.parkonect.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.features.ContentConverter
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining
import mu.KotlinLogging
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
                    var bodyText = call.receiveText()
                    bodyText = if (bodyText.lines()[0].startsWith("<?xml version")) {
                        val lst = bodyText.lines().toMutableList()
                        lst.removeAt(0)
                        lst.joinToString("\n")
                    } else {
                        bodyText
                    }

                    val reqBody = XmlConverter.objMapper.readValue(bodyText, OnDemandEntryRequest::class.java)
                    log.info("Parsed body: $reqBody")
                    val respBody = OnDemandEntryResponse(true)
                    XmlConverter.objMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    XmlConverter.objMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                    val outputText = XmlConverter.objMapper.writeValueAsString(reqBody)
                    call.respondText(outputText, ContentType.Application.Xml, HttpStatusCode.OK)
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

@JacksonXmlRootElement(localName = "Entry")
class OnDemandEntryRequest {
    @field:JacksonXmlProperty(localName = "GarageID")
    var garageId: Int
    @field:JacksonXmlProperty(localName = "Barcode")
    var barcode: String
    @field:JacksonXmlProperty(localName = "LaneID")
    var laneId: Int
    @field:JacksonXmlProperty(localName = "ActualEntryTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = XmlConverter.DATE_FORMAT_PATTERN, timezone = "UTC")
    var entryTime: Date = Date()

    @JvmOverloads
    constructor(garageId: Int = 0, barcode: String = "", laneId: Int = 0, entryTime: Date = Date()) {
        this.garageId = garageId
        this.barcode = barcode
        this.laneId = laneId
        this.entryTime = entryTime
    }

}

@JacksonXmlRootElement(localName = "Result")
class OnDemandEntryResponse {
    @field:JacksonXmlProperty(localName = "Success")
    var success: Boolean
    @field:JacksonXmlProperty(localName = "Message")
    var message: String? = null

    @JvmOverloads
    constructor(success: Boolean = true, message: String? = null) {
        this.success = success
        this.message = message
    }

    constructor(message: String) {
        this.success = false
        this.message = message
    }
}


@JacksonXmlRootElement(localName = "Exit")
class OnDemandExitRequest {
    @field:JacksonXmlProperty(localName = "GarageID")
    var garageId: Int
    @field:JacksonXmlProperty(localName = "Barcode")
    var barcode: String
    @field:JacksonXmlProperty(localName = "LaneID")
    var laneId: Int
    @field:JacksonXmlProperty(localName = "Amount")
    var amount: Float
    @field:JacksonXmlProperty(localName = "ActualExitTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = XmlConverter.DATE_FORMAT_PATTERN, timezone = "UTC")
    var entryTime: Date

    @JvmOverloads
    constructor(
        garageId: Int = 0,
        barcode: String = "",
        laneId: Int = 0,
        amount: Float = 0f,
        entryTime: Date = Date()
    ) {
        this.garageId = garageId
        this.barcode = barcode
        this.laneId = laneId
        this.amount = amount
        this.entryTime = entryTime
    }
}

@JacksonXmlRootElement(localName = "Result")
class OnDemandExitResponse {
    @field:JacksonXmlProperty(localName = "Success")
    var success: Boolean
    @field:JacksonXmlProperty(localName = "TransactionID")
    var trasnactionId: Long?
    @field:JacksonXmlProperty(localName = "Message")
    var message: String?

    @JvmOverloads
    constructor(success: Boolean = true, transId: Long = 0) {
        this.success = success
        this.trasnactionId = transId
        this.message = null
    }

    constructor(message: String) {
        this.success = false
        this.trasnactionId = null
        this.message = message
    }

}

class XmlConverter : ContentConverter {
    companion object {
        val log = KotlinLogging.logger(XmlConverter::class.java.simpleName)
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss'Z'"
        val objMapper = XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        })
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null
        val xmlValue = channel.readRemaining().readText().replace("<?xml version=”1.0” encoding=”UTF-8”?>", "")
        log.info { "XML:\n$xmlValue" }
        return objMapper.readValue(xmlValue, request.type.javaObjectType)
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return TextContent(objMapper.writeValueAsString(value), contentType.withCharset(context.call.suitableCharset()))
    }

}