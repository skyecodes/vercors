package app.vercors.feature.project.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MrpackIndex(
    val name: String,
    val files: List<MrpackFile> = emptyList(),
    val dependencies: Map<String, String> = emptyMap()
)

@Serializable
internal data class MrpackFile(
    val path: String,
    val hashes: MrpackHashes = MrpackHashes(),
    val env: MrpackEnv? = null,
    val downloads: List<String> = emptyList(),
    val fileSize: Long? = null
)

@Serializable
internal data class MrpackHashes(val sha1: String? = null)

@Serializable
internal data class MrpackEnv(val client: String = "required", val server: String = "required")
