package app.vercors.feature.project.data.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class MrpackIndex(
    @EncodeDefault val formatVersion: Int = 1,
    @EncodeDefault val game: String = "minecraft",
    @EncodeDefault val versionId: String = "",
    val name: String,
    val summary: String? = null,
    @EncodeDefault val files: List<MrpackFile> = emptyList(),
    @EncodeDefault val dependencies: Map<String, String> = emptyMap()
)

@Serializable
internal data class MrpackFile(
    val path: String,
    val hashes: MrpackHashes = MrpackHashes(),
    val env: MrpackEnv? = null,
    @EncodeDefault val downloads: List<String> = emptyList(),
    val fileSize: Long? = null
)

@Serializable
internal data class MrpackHashes(val sha1: String? = null, val sha512: String? = null)

@Serializable
internal data class MrpackEnv(
    @EncodeDefault val client: String = "required",
    @EncodeDefault val server: String = "required"
)
