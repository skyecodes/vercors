package app.vercors.meta.backend.project.curseforge.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

typealias CurseForgeInstant = @Serializable(CurseforgeInstantSerializer::class) Instant

private class CurseforgeInstantSerializer : KSerializer<CurseForgeInstant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CurseforgeInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CurseForgeInstant = Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: CurseForgeInstant) {
        // Not necessary
    }
}