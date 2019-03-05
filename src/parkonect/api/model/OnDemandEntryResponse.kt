package parkonect.api.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

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