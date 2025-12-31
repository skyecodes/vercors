package app.vercors.feature.instance.domain.model

import app.vercors.core.domain.toSlug
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class Instance(
    val name: String,
    val id: InstanceId = name.toSlug(),
    val gameVersion: String,
    val modLoader: InstanceModLoader? = null,
    val modpackInfo: InstanceModpackInfo? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val lastPlayedAt: Instant? = null,
    val playTime: Duration = Duration.ZERO,
) {
    @Deprecated("Move to UI")
    val loaderName: String get() = modLoader?.type?.name ?: "Vanilla"

    @Deprecated("Move to UI")
    val typeName: String get() = if (modpackInfo != null) "Modpack" else "Custom"
}

typealias InstanceId = String
