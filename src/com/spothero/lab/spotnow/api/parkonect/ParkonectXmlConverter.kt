package com.spothero.lab.spotnow.api.parkonect

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import io.ktor.application.ApplicationCall
import io.ktor.features.ContentConverter
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining
import java.text.SimpleDateFormat

class ParkonectXmlConverter : ContentConverter {
    companion object {

        val objMapper = XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        }).apply {
            dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
            configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        }
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null
        val xmlValue = channel.readRemaining().readText()
        return objMapper.readValue(xmlValue, request.type.javaObjectType)
    }

    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return TextContent(objMapper.writeValueAsString(value), contentType)
    }

}