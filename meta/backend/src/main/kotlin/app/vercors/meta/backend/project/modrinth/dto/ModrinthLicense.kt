package app.vercors.meta.backend.project.modrinth.dto


import kotlinx.serialization.Serializable

@Serializable
data class ModrinthLicense(
    val id: String,
    val name: String,
    val url: String? = null
)