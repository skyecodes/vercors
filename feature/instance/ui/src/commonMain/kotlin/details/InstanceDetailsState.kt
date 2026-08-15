package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.*
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Instant

@Immutable
data class InstanceDetailsState(
    val instance: ProgressiveOutcome<InstanceError, InstanceView>,
    val editedInstance: InstanceEditState,
    val gameVersionList: List<GameVersionView>,
    val loaderList: List<ModLoaderType>,
    val loaderVersionList: List<LoaderVersion>,
    val selectedTab: InstanceDetailsTab,
    val promptDeleteConfirmation: Boolean,
    val logs: List<InstanceLogEventView>?,
) {
    val isNameEdited = instance.valueOrNull?.let {
        editedInstance.name != null && editedInstance.name != it.name
    } ?: false
    val isGameVersionEdited = instance.valueOrNull?.let {
        editedInstance.gameVersion != null && editedInstance.gameVersion != it.gameVersion
    } ?: false
    val isModLoaderEdited = instance.valueOrNull?.let {
        val current: InstanceModLoaderEdit = it.loaderType?.let { type ->
            InstanceModLoaderEdit.Loader(type, it.loaderVersion.orEmpty())
        } ?: InstanceModLoaderEdit.Vanilla
        editedInstance.modLoader != null && editedInstance.modLoader != current
    } ?: false
    val isInstanceEdited: Boolean = isNameEdited || isGameVersionEdited || isModLoaderEdited
}

enum class InstanceDetailsTab {
    Options,
    Mods,
    ResourcePacks,
    ShaderPacks,
    DataPacks,
    Worlds,
    Logs
}

@Immutable
data class InstanceView(
    val id: InstanceId,
    val name: String,
    val gameVersion: String,
    val loader: String,
    val loaderType: ModLoaderType?,
    val loaderVersion: String?,
    val type: String,
    val status: InstanceStatus,
)

@Immutable
data class InstanceEditState(
    val name: String? = null,
    val gameVersion: String? = null,
    val modLoader: InstanceModLoaderEdit? = null,
)

@Immutable
sealed interface InstanceModLoaderEdit {
    data object Vanilla : InstanceModLoaderEdit
    data class Loader(val type: ModLoaderType, val version: String) : InstanceModLoaderEdit
}

@Immutable
data class InstanceLogEventView(
    val timestamp: Instant,
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
    gameVersion = instance.gameVersion,
    loader = instance.modLoader?.type.displayName,
    loaderType = instance.modLoader?.type,
    loaderVersion = instance.modLoader?.version,
    type = instance.displayType,
    status = status
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"

fun InstanceLogEvent.toView(): InstanceLogEventView = when (this) {
    is InstanceLogEvent.GameLog4j -> InstanceLogEventView(
        timestamp = inst,
        level = InstanceLogEventView.Level.valueOf(level.uppercase()),
        message = toString(),
        isLauncher = false
    )

    is InstanceLogEvent.Plain -> InstanceLogEventView(
        timestamp = inst,
        level = if (this is InstanceLogEvent.Error) InstanceLogEventView.Level.ERROR else InstanceLogEventView.Level.INFO,
        message = toString(),
        isLauncher = this is InstanceLogEvent.Launcher
    )
}
