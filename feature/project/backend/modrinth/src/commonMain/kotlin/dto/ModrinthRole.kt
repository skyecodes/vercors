package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthRole {
    @SerialName("admin")
    Admin,

    @SerialName("moderator")
    Moderator,

    @SerialName("developer")
    Developer
}
