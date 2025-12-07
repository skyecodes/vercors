package app.vercors.meta.backend.project.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthProjectMember(
    val role: String,
    @SerialName("team_id")
    val teamId: String,
    val user: ModrinthUser,
    val accepted: Boolean,
    @SerialName("payouts_split")
    val payoutsSplit: Int? = null,
    val permissions: Int? = null,
)
