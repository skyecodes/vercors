package app.vercors.feature.instance.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.InstanceAndStatus
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.details.displayType
import kotlin.time.Instant

@Immutable
data class BriefInstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String,
    val iconFilePath: String?,
    val status: InstanceStatus,
    val updatedAt: Instant
)

fun InstanceAndStatus.toBriefView(): BriefInstanceView = BriefInstanceView(
    id = instance.id,
    name = instance.name,
    version = instance.gameVersion,
    loader = instance.modLoader?.type.displayName,
    type = instance.displayType,
    iconFilePath = instance.iconFilePath,
    status = status,
    updatedAt = instance.updatedAt
)
