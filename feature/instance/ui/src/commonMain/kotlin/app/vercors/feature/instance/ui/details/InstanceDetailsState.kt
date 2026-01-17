package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Resource
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.*

@Immutable
data class InstanceDetailsState(
    val instance: Resource<InstanceView> = Resource.Unloaded,
    val logs: List<InstanceLogEventView>? = null,
)

@Immutable
data class InstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String,
    val status: InstanceStatus,
)

@Immutable
data class InstanceLogEventView(
    val level: Level,
    val message: String,
    val isLauncher: Boolean,
) {
    enum class Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}

fun InstanceAndStatus.toView(): InstanceView = InstanceView(
    id = instance.id,
    name = instance.name,
    version = instance.gameVersion,
    loader = instance.modLoader?.type.displayName,
    type = instance.displayType,
    status = status
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"

fun InstanceLogEvent.toView(): InstanceLogEventView = when (this) {
    is InstanceLogEvent.GameLog4j -> InstanceLogEventView(
        level = InstanceLogEventView.Level.valueOf(level.uppercase()),
        message = toString(),
        isLauncher = false
    )

    is InstanceLogEvent.Plain -> InstanceLogEventView(
        level = if (this is InstanceLogEvent.Error) InstanceLogEventView.Level.ERROR else InstanceLogEventView.Level.INFO,
        message = toString(),
        isLauncher = this is InstanceLogEvent.Launcher
    )
}
