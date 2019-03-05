package com.spothero.lab

import com.spothero.lab.parkonect.api.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SerializationTest {


    @Test
    fun testEntryRequestSerialization() {
        var request = OnDemandEntryRequest(
            44, "BARCODE-test1", 30,
            DateTime(2017, 2, 5, 13, 15, 10, DateTimeZone.UTC).toDate()
        )
        var xmlString = XmlConverter.objMapper.writeValueAsString(request)

        println(xmlString)

        assert(xmlString.contains("<Entry>", false))
        assert(xmlString.contains("<GarageID>", false))
        assert(xmlString.contains("<Barcode>", false))
        assert(xmlString.contains("<LaneID>", false))
        assert(xmlString.contains("<ActualEntryTime>2017-02-05 13:15:10Z</ActualEntryTime>", false))

        var requestB = XmlConverter.objMapper.readValue(xmlString, OnDemandEntryRequest::class.java)

        assertEquals(request.garageId, requestB.garageId)
        assertEquals(request.barcode, requestB.barcode)
        assertEquals(request.laneId, requestB.laneId)
        assertEquals(request.entryTime, requestB.entryTime)

    }

    @Test
    fun testEntryResponseSerialization() {
        var response = OnDemandEntryResponse(true)
        var xmlString = XmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>true</Success>", false))

        var responseB = XmlConverter.objMapper.readValue(xmlString, OnDemandEntryResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertNull(response.message)

        response = OnDemandEntryResponse(false)
        xmlString = XmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>false</Success>", false))

        responseB = XmlConverter.objMapper.readValue(xmlString, OnDemandEntryResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertNull(response.message)

        response = OnDemandEntryResponse("Some error message")
        xmlString = XmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>false</Success>", false))
        assert(xmlString.contains("<Message>Some error message</Message>", false))

        responseB = XmlConverter.objMapper.readValue(xmlString, OnDemandEntryResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertEquals(response.message, responseB.message)
    }


    @Test
    fun testExitRequestSerialization() {
        var request = OnDemandExitRequest(
            44, "BARCODE-test1", 30, 10f,
            DateTime(2017, 2, 5, 13, 15, 10, DateTimeZone.UTC).toDate()
        )
        var xmlString = XmlConverter.objMapper.writeValueAsString(request)

        println(xmlString)

        assert(xmlString.contains("<Exit>", false))
        assert(xmlString.contains("<GarageID>", false))
        assert(xmlString.contains("<Barcode>", false))
        assert(xmlString.contains("<LaneID>", false))
        assert(xmlString.contains("<Amount>", false))
        assert(xmlString.contains("<ActualExitTime>2017-02-05 13:15:10Z</ActualExitTime>", false))

        var requestB = XmlConverter.objMapper.readValue(xmlString, OnDemandExitRequest::class.java)

        assertEquals(request.garageId, requestB.garageId)
        assertEquals(request.barcode, requestB.barcode)
        assertEquals(request.laneId, requestB.laneId)
        assertEquals(request.amount, requestB.amount)
        assertEquals(request.entryTime, requestB.entryTime)
    }

    @Test
    fun testExitResponseSerialization() {
        val transID = 3423423423434L

        var response = OnDemandExitResponse(true, transID)
        var xmlString = XmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>true</Success>", false))
        assert(xmlString.contains("<TransactionID>3423423423434</TransactionID>", false))

        var responseB = XmlConverter.objMapper.readValue(xmlString, OnDemandExitResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertEquals(response.trasnactionId, responseB.trasnactionId)
        assertNull(response.message)

        response = OnDemandExitResponse("Some error message")
        xmlString = XmlConverter.objMapper.writeValueAsString(response)
        println(xmlString)

        assert(xmlString.contains("<Result>", false))
        assert(xmlString.contains("<Success>false</Success>", false))
        assert(xmlString.contains("<Message>Some error message</Message>", false))

        responseB = XmlConverter.objMapper.readValue(xmlString, OnDemandExitResponse::class.java)
        assertEquals(response.success, responseB.success)
        assertEquals(response.message, responseB.message)
    }
}