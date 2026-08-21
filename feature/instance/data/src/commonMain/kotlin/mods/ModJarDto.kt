package app.vercors.feature.instance.data.mods

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
internal data class FabricModJson(
    val id: String,
    val name: String? = null,
    val version: String = "unknown",
    val description: String? = null,
    val icon: JsonElement? = null
)

@Serializable
internal data class QuiltModJson(
    @SerialName("quilt_loader") val quiltLoader: QuiltLoaderSection
) {
    @Serializable
    data class QuiltLoaderSection(
        val id: String,
        val version: String = "unknown",
        val metadata: QuiltMetadata? = null
    )

    @Serializable
    data class QuiltMetadata(
        val name: String? = null,
        val description: String? = null,
        val icon: JsonElement? = null
    )
}

/** Forge/NeoForge `mods.toml` / `neoforge.mods.toml` — both use the same `[[mods]]` array-of-tables shape. */
@Serializable
internal data class ForgeModsToml(
    val logoFile: String? = null,
    val mods: List<ForgeModEntry> = emptyList()
)

@Serializable
internal data class ForgeModEntry(
    val modId: String,
    val version: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val logoFile: String? = null
)

/** Legacy Forge `mcmod.info` (Minecraft 1.12.2 and earlier) — a bare JSON array of mod entries. */
@Serializable
internal data class McModInfoEntry(
    val modid: String? = null,
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    val logoFile: String? = null
)

/** Fabric/Quilt icon fields are either a single path, or a map of icon-size to path — pick the largest. */
internal fun JsonElement?.iconPathOrNull(): String? = when (this) {
    null -> null
    is JsonPrimitive -> contentOrNull
    is JsonObject -> entries.maxByOrNull { it.key.toIntOrNull() ?: 0 }
        ?.value?.let { (it as? JsonPrimitive)?.contentOrNull }

    else -> null
}
