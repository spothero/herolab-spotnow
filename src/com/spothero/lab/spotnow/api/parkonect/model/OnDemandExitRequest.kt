package com.spothero.lab.spotnow.api.parkonect.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.spothero.lab.spotnow.api.parkonect.ParkonectXmlConverter
import java.util.*

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ParkonectXmlConverter.DATE_FORMAT_PATTERN, timezone = "UTC")
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