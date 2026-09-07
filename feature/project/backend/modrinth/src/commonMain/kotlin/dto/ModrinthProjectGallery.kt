package app.vercors.feature.project.backend.modrinth.dto


import kotlinx.serialization.Serializable

@Serializable
data class ModrinthProjectGallery(
    val created: ModrinthInstant,
    val description: String? = null,
    val featured: Boolean,
    val ordering: Int,
    val title: String? = null,
    val url: String
)
