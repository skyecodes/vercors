package app.vercors.feature.project.backend.modrinth.dto


import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ModrinthProjectGallery(
    val created: Instant,
    val description: String? = null,
    val featured: Boolean,
    val ordering: Int,
    val title: String? = null,
    val url: String
)
