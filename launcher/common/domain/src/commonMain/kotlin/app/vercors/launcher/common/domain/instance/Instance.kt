package app.vercors.launcher.common.domain.instance

import app.vercors.shared.domain.toSlug
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class Instance(
    val name: String,
    val id: String = name.toSlug(),
    val gameVersion: String,
    val modLoader: InstanceModLoader? = null,
    val modpackInfo: InstanceModpackInfo? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val lastPlayedAt: Instant? = null,
    val playTime: Duration = Duration.ZERO,
)

