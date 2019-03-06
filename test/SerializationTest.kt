package com.spothero.lab

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import parkonect.api.ParkonectXmlConverter
import parkonect.api.model.OnDemandEntryRequest
import parkonect.api.model.OnDemandEntryResponse
import parkonect.api.model.OnDemandExitRequest
import parkonect.api.model.OnDemandExitResponse
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SerializationTest {


    @Test
    fun testEntryRequestSerialization() {
        var request = OnDemandEntryRequest(
            44, "BARCODE-test1", 30,
            DateTime(2017, 2, 5, 13, 15, 10, DateTimeZone.UTC).toDate()
        )
        var xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(request)

        println(xmlString)

        assert(xmlString.contains("<Entry>", false))
        assert(xmlString.contains("<GarageID>", false))
        assert(xmlString.contains("<Barcode>", false))
        assert(xmlString.contains("<LaneID>", false))
        assert(xmlString.contains("<ActualEntryTime>2017-02-05 13:15:10Z</ActualEntryTime>", false))

        var requestB = ParkonectXmlConverter.objMapper.readValue(
            xmlString,
            OnDemandEntryRequest::class.java
        )

        assertEquals(request.garageId, requestB.garageId)
        assertEquals(request.barcode, requestB.barcode)
        assertEquals(request.laneId, requestB.laneId)
        assertEquals(request.entryTime, requestB.entryTime)

    }

    @Test
    fun testEntryResponseSerialization() {
        var response = OnDemandEntryResponse(true)
        var xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>true</Success>", false))

        var responseB = ParkonectXmlConverter.objMapper.readValue(xmlString, OnDemandEntryResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertNull(response.message)

        response = OnDemandEntryResponse(false)
        xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>false</Success>", false))

        responseB = ParkonectXmlConverter.objMapper.readValue(xmlString, OnDemandEntryResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertNull(response.message)

        response = OnDemandEntryResponse("Some error message")
        xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>false</Success>", false))
        assert(xmlString.contains("<Message>Some error message</Message>", false))

        responseB = ParkonectXmlConverter.objMapper.readValue(xmlString, OnDemandEntryResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertEquals(response.message, responseB.message)
    }


    @Test
    fun testExitRequestSerialization() {
        var request = OnDemandExitRequest(
            44, "BARCODE-test1", 30, 10f,
            DateTime(2017, 2, 5, 13, 15, 10, DateTimeZone.UTC).toDate()
        )
        var xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(request)

        println(xmlString)

        assert(xmlString.contains("<Exit>", false))
        assert(xmlString.contains("<GarageID>", false))
        assert(xmlString.contains("<Barcode>", false))
        assert(xmlString.contains("<LaneID>", false))
        assert(xmlString.contains("<Amount>", false))
        assert(xmlString.contains("<ActualExitTime>2017-02-05 13:15:10Z</ActualExitTime>", false))

        var requestB = ParkonectXmlConverter.objMapper.readValue(xmlString, OnDemandExitRequest::class.java)

        assertEquals(request.garageId, requestB.garageId)
        assertEquals(request.barcode, requestB.barcode)
        assertEquals(request.laneId, requestB.laneId)
        assertEquals(request.amount, requestB.amount)
        assertEquals(request.entryTime, requestB.entryTime)
    }

    @Test
    fun testExitResponseSerialization() {
        val transID = "489efb90-84cb-496f-be82-161c416b968f"

        var response = OnDemandExitResponse(true, transID)
        var xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>true</Success>", false))
        assert(xmlString.contains("<TransactionID>489efb90-84cb-496f-be82-161c416b968f</TransactionID>", false))

        var responseB = ParkonectXmlConverter.objMapper.readValue(xmlString, OnDemandExitResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertEquals(response.trasnactionId, responseB.trasnactionId)
        assertNull(response.message)

        response = OnDemandExitResponse(false, null, "Some error message")
        xmlString = ParkonectXmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>false</Success>", false))
        assert(xmlString.contains("<Message>Some error message</Message>", false))

        responseB = ParkonectXmlConverter.objMapper.readValue(xmlString, OnDemandExitResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertEquals(response.message, responseB.message)
    }
}