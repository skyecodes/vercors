package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class InstallProfileData(
    val data: Map<String, SidedValue> = emptyMap(),
    val processors: List<Processor> = emptyList(),
    val libraries: List<MojangVersionInfo.Library> = emptyList()
) {
    @Serializable
    data class SidedValue(val client: String, val server: String? = null)

    @Serializable
    data class Processor(
        val sides: List<String>? = null,
        val jar: String,
        val classpath: List<String> = emptyList(),
        val args: List<String> = emptyList()
    )
}
