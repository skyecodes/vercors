package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoaderVersionProfile(
    val mainClass: String,
    val arguments: MojangVersionInfo.Arguments? = null,
    val libraries: List<MojangVersionInfo.Library> = emptyList(),
    // Produced locally by FML installers: never on the classpath, but checked before trusting a cached profile
    val localArtifacts: List<MojangVersionInfo.Library.Downloads.Artifact> = emptyList()
)
