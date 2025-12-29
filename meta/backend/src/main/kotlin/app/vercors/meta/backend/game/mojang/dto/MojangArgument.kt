package app.vercors.meta.backend.game.mojang.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable(ArgumentSerializer::class)
sealed interface MojangArgument

@Serializable
data class BasicArgument(
    val value: String
) : MojangArgument

@Serializable
data class ConditionalArgument(
    val rules: List<MojangVersionInfo.Rule>,
    val value: List<String>
) : MojangArgument

internal class ArgumentSerializer : KSerializer<MojangArgument> {
    override val descriptor = buildClassSerialDescriptor("MojangArgument")

    override fun deserialize(decoder: Decoder): MojangArgument {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        return if (element is JsonPrimitive) BasicArgument(element.content)
        else {
            val obj = element.jsonObject
            val rules = obj["rules"]?.jsonArray
                ?.map { jsonDecoder.json.decodeFromJsonElement(MojangVersionInfo.Rule.serializer(), it.jsonObject) }
            val valueJson = obj["value"]
            val value =
                if (valueJson is JsonArray) valueJson.map { it.jsonPrimitive.content }
                else listOf(valueJson!!.jsonPrimitive.content)
            ConditionalArgument(rules!!, value)
        }
    }

    override fun serialize(encoder: Encoder, value: MojangArgument) {
        val jsonEncoder = encoder as JsonEncoder
        when (value) {
            is BasicArgument -> jsonEncoder.encodeString(value.value)
            is ConditionalArgument -> jsonEncoder.encodeJsonElement(buildJsonObject {
                putJsonArray("rules") { value.rules.forEach { add(jsonEncoder.json.encodeToJsonElement(it)) } }
                putJsonArray("value") { value.value.forEach { add(it) } }
            })
        }
    }
}
