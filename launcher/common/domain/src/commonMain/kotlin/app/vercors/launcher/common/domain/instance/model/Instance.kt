package app.vercors.launcher.common.domain.instance.model

import app.vercors.shared.domain.toSlug
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
    val loaderName: String get() = modLoader?.type?.name ?: "Vanilla"
    val typeName: String get() = modpackInfo?.platform?.name ?: "Custom"
}

typealias InstanceId = String
