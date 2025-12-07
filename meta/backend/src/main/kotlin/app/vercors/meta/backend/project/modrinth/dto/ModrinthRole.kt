package app.vercors.meta.backend.project.modrinth.dto

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
