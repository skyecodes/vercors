package app.vercors.feature.launcher.data.dto

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(value = "Event")
data class Log4jEvent(
    val logger: String,
    val timestamp: Long,
    val level: String,
    val thread: String,
    @XmlElement
    @XmlSerialName(value = "Message")
    val message: String,
    @XmlElement
    @XmlSerialName(value = "Throwable")
    val throwable: String? = null
)
