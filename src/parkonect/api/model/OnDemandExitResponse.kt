package parkonect.api.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

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