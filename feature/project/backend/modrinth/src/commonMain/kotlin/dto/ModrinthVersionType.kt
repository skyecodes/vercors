package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ModrinthVersionType(val value: String) {
    companion object {
        const val RELEASE = "release"
        const val BETA = "beta"
        const val ALPHA = "alpha"
    }
}
