package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

/**
 * A Forge/NeoForge-family installer's `install_profile.json`: libraries to download plus an ordered list of
 * "processors" that must actually be executed (each a `java -cp <classpath> <Main-Class> <args>` invocation)
 * to produce the loader's real libraries/patched client jar, whose final shape is described by the installer's
 * separate `version.json` (see [LoaderVersionProfile]).
 */
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
