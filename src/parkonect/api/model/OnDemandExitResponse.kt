package parkonect.api.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "Result")
class OnDemandExitResponse {
    @field:JacksonXmlProperty(localName = "Success")
    var success: Boolean
    @field:JacksonXmlProperty(localName = "TransactionID")
    var trasnactionId: String?
    @field:JacksonXmlProperty(localName = "Message")
    var message: String?

    @JvmOverloads
    constructor(success: Boolean = true, transId: String? = null, message: String? = null) {
        this.success = success
        this.trasnactionId = transId
        this.message = message
    }
}