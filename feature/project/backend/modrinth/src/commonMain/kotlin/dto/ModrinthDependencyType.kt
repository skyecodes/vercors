package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ModrinthDependencyType(val value: String) {
    companion object {
        const val REQUIRED = "required"
    }
}
