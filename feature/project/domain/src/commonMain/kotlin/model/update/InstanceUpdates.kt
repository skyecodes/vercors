package app.vercors.feature.project.domain.model.update

import kotlin.time.Instant

data class InstanceUpdates(
    val checkedAt: Instant,
    val modpack: ModpackUpdate?,
    val content: List<ContentUpdate>
) {
    val hasUpdates: Boolean get() = modpack != null || content.isNotEmpty()
}
