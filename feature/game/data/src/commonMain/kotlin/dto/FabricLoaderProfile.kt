package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class FabricLoaderProfile(
    val mainClass: String,
    val arguments: MojangVersionInfo.Arguments? = null,
    val libraries: List<Library> = emptyList()
) {
    @Serializable
    data class Library(
        val name: String,
        val url: String,
        val sha1: String? = null,
        val size: Long? = null
    )
}
