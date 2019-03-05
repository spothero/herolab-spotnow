package com.spothero.lab

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import parkonect.api.ParkonectXmlConverter
import parkonect.api.model.OnDemandEntryRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("HELLO WORLD!", response.content)
            }
        }
    }

    @Test
    fun testXmlParsing() {

        val module = JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        }
        val objMapper = XmlMapper(module)
        val bodyText = "<Entry>\n" +
                "\t<GarageID>391</GarageID> \n" +
                "\t<Barcode>VendorSpecific</Barcode>\n" +
                "\t<LaneID>1</LaneID>\n" +
                "\t<ActualEntryTime>2015-12-25 13:15:20Z</ActualEntryTime>\n" +
                "</Entry>"

        val reqBody = objMapper.readValue(bodyText, OnDemandEntryRequest::class.java)


        val outputText = ParkonectXmlConverter.objMapper.writeValueAsString(reqBody)
        System.out.println("XML:\n$outputText")
        assertEquals(bodyText, outputText)
    }
}
