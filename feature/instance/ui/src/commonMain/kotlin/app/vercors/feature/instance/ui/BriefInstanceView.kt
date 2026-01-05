package app.vercors.feature.instance.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.details.displayType

@Immutable
data class BriefInstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String
)

fun Instance.toBriefView(): BriefInstanceView = BriefInstanceView(
    id = id,
    name = name,
    version = gameVersion,
    loader = modLoader?.type.displayName,
    type = displayType
)
