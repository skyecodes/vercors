package app.vercors.meta.backend.project.modrinth.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

typealias ModrinthInstant = @Serializable(ModrinthInstantSerializer::class) Instant

internal class ModrinthInstantSerializer : KSerializer<ModrinthInstant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ModrinthInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ModrinthInstant = Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ModrinthInstant) {
        // Not necessary
    }
}
