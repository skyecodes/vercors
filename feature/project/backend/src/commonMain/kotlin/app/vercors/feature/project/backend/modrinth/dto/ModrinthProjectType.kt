package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ModrinthProjectType(val value: String) {
    companion object {
        const val MOD = "mod"
        const val MODPACK = "modpack"
        const val RESOURCEPACK = "resourcepack"
        const val SHADERPACK = "shader"
        const val DATAPACK = "datapack"
    }
}
