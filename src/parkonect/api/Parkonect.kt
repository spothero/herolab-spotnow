package com.spothero.lab.parkonect.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.deser.std.DateDeserializers
import com.fasterxml.jackson.databind.ser.std.DateSerializer
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser
import io.ktor.application.*
import io.ktor.auth.authenticate
import io.ktor.features.ContentConverter
import io.ktor.features.ContentNegotiation
import io.ktor.features.conversionService
import io.ktor.features.suitableCharset
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.receive
import io.ktor.request.receiveText
import io.ktor.response.defaultTextContentType
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining
import mu.KotlinLogging
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement


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
                        val lst=bodyText.lines().toMutableList()
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
data class OnDemandEntryRequest(
    @JacksonXmlProperty(localName = "GarageID")
    val garageId: Int,
    @JacksonXmlProperty(localName = "Barcode")
    val barcode: String,
    @JacksonXmlProperty(localName = "LaneID")
    val laneId: Int,
    @JacksonXmlProperty(localName = "ActualEntryTime")
    val entryTime: Date
)

@JacksonXmlRootElement(localName = "Result")
data class OnDemandEntryResponse(
    @JacksonXmlProperty(localName = "Success")
    val success: Boolean,
    @JacksonXmlProperty(localName = "Message")
    val message: String? = null
)

@JacksonXmlRootElement(localName = "Exit")
data class OnDemandExitRequest(
    @JacksonXmlProperty(localName = "GarageID")
    val garageId: Int,
    @JacksonXmlProperty(localName = "Barcode")
    val barcode: String,
    @JacksonXmlProperty(localName = "LaneID")
    val laneId: Int,
    @JacksonXmlProperty(localName = "Amount")
    val amount: Float,
    @JacksonXmlProperty(localName = "ActualExitTime")
    val entryTime: Date
)

@JacksonXmlRootElement(localName = "Result")
data class OnDemandExitResponse(
    @JacksonXmlProperty(localName = "Success")
    val success: Boolean,
    @JacksonXmlProperty(localName = "TransactionID")
    val trasnactionId: Long,
    @JacksonXmlProperty(localName = "Message")
    val message: String? = null
)

class XmlConverter : ContentConverter {
    companion object {
        val log = KotlinLogging.logger(XmlConverter::class.java.simpleName)

        val dateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss'Z'").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateSerializer = DateSerializer.instance.withFormat(true, dateFormat)

        val module = JacksonXmlModule().apply {
            this.addSerializer(Date::class.java, dateSerializer)
            DateDeserializers.DateDeserializer.instance
            this.addDeserializer(
                Date::class.java,
                DateDeserializers.DateDeserializer(
                    DateDeserializers.DateDeserializer.instance,
                    dateFormat,
                    dateFormat.toPattern()
                )
            )
            setDefaultUseWrapper(false)

        }
        val objMapper = XmlMapper(module).apply {
            this.dateFormat = dateFormat
            this.setTimeZone(TimeZone.getTimeZone("UTC"))
        }
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