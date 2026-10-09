package app.vercors.feature.game.backend.loader.dto

import kotlinx.serialization.Serializable

@Serializable
data class MetaLoaderVersionInfo(val version: String, val stable: Boolean? = null) {
    // Quilt's meta API never sends `stable` (unlike Fabric's), fall back to a version-string heuristic
    val effectiveStable: Boolean get() = stable ?: !version.contains("-beta")
}
