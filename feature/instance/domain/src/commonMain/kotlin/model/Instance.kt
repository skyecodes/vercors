package app.vercors.feature.instance.domain.model

import app.vercors.core.domain.toSlug
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

data class Instance(
    val name: String,
    val id: InstanceId = name.toSlug(),
    val gameVersion: String,
    val modLoader: InstanceModLoader? = null,
    val modpackInfo: InstanceModpackInfo? = null,
    val pendingManualDownloads: List<PendingManualDownload> = emptyList(),
    val installStage: InstanceInstallStage? = null,
    val iconFilePath: String? = null,
    val javaInstallationId: String? = null,
    val jvmMemoryOptions: JvmMemoryOptions? = null,
    val nativeLibraryOptions: NativeLibraryOptions? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val lastPlayedAt: Instant? = null,
    val playTime: Duration = Duration.ZERO
)

typealias InstanceId = String
