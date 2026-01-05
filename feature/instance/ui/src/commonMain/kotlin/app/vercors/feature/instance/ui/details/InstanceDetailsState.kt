package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Resource
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId

@Immutable
data class InstanceDetailsState(
    val instance: Resource<InstanceView> = Resource.Unloaded,
)

@Immutable
data class InstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String,
)

fun Instance.toView(): InstanceView = InstanceView(
    id = id,
    name = name,
    version = gameVersion,
    loader = modLoader?.type.displayName,
    type = displayType,
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"
