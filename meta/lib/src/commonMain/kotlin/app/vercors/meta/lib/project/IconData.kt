package app.vercors.meta.lib.project

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.jvm.JvmInline

@Serializable(IconDataSerializer::class)
sealed interface IconData

@JvmInline
value class IconSvgData(val svgString: String) : IconData

@JvmInline
value class IconNameData(val name: String) : IconData

private class IconDataSerializer : KSerializer<IconData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("IconData") {
        element<String>("name", isOptional = true)
        element<String>("data", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: IconData) {
        val encoder = encoder as JsonEncoder
        encoder.encodeJsonElement(buildJsonObject {
            when (value) {
                is IconNameData -> put("name", value.name)
                is IconSvgData -> put("data", value.svgString)
            }
        })
    }

    override fun deserialize(decoder: Decoder): IconData {
        val decoder = decoder as JsonDecoder
        val obj = decoder.decodeJsonElement().jsonObject
        return if (obj.containsKey("name")) IconNameData(obj.getValue("name").jsonPrimitive.content)
        else IconSvgData(obj.getValue("data").jsonPrimitive.content)
    }

}
