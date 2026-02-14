package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthProjectStatus {
    @SerialName("approved")
    Approved,

    @SerialName("archived")
    Archived,

    @SerialName("rejected")
    Rejected,

    @SerialName("draft")
    Draft,

    @SerialName("unlisted")
    Unlisted,

    @SerialName("processing")
    Processing,

    @SerialName("withheld")
    Withheld,

    @SerialName("scheduled")
    Scheduled,

    @SerialName("private")
    Private,

    @SerialName("unknown")
    Unknown
}
