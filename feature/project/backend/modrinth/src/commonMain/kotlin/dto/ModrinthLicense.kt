package app.vercors.feature.project.backend.modrinth.dto


import kotlinx.serialization.Serializable

@Serializable
data class ModrinthLicense(
    val id: String,
    val name: String,
    val url: String? = null
)
