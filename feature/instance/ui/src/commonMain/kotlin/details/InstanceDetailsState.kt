package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.ui.install.FinishInstallationDialog
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
    val installedMods: List<InstalledContentFileView>?,
    val installedResourcePacks: List<InstalledContentFileView>?,
    val installedShaderPacks: List<InstalledContentFileView>?,
    val promptRemoveConfirmation: PendingRemoval?,
    val promptFinishInstallation: Boolean,
    val finishInstallationDialog: FinishInstallationDialog,
) {
    val isNameEdited = instance.valueOrNull?.let {
        editedInstance.name != null && editedInstance.name != it.name
    } ?: false
    val isGameVersionEdited = instance.valueOrNull?.let {
        editedInstance.gameVersion != null && editedInstance.gameVersion != it.gameVersion
    } ?: false
    val isLoaderTypeEdited = instance.valueOrNull?.let {
        val current: ModLoaderTypeEdit = it.loaderType?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla
        editedInstance.modLoaderType != null && editedInstance.modLoaderType != current
    } ?: false
    val isLoaderVersionEdited = instance.valueOrNull?.let {
        editedInstance.modLoaderVersion != null && editedInstance.modLoaderVersion != it.loaderVersion
    } ?: false
    val isModLoaderEdited = isLoaderTypeEdited || isLoaderVersionEdited
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
    val modpackInfo: InstanceModpackInfo?,
)

@Immutable
data class InstanceEditState(
    val name: String? = null,
    val gameVersion: String? = null,
    val modLoaderType: ModLoaderTypeEdit? = null,
    val modLoaderVersion: String? = null,
)

@Immutable
sealed interface ModLoaderTypeEdit {
    data object Vanilla : ModLoaderTypeEdit

    @JvmInline
    value class Loader(val type: ModLoaderType) : ModLoaderTypeEdit
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
    status = status,
    modpackInfo = instance.modpackInfo
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"

@Immutable
data class InstalledContentFileView(
    val fileName: String,
    val title: String,
    val description: String?,
    val version: String?,
    val loaderLabel: String?,
    val iconFilePath: String?,
    val enabled: Boolean,
    val providers: List<ModProvider>,
)

fun InstalledContentFile.toView(): InstalledContentFileView = InstalledContentFileView(
    fileName = fileName,
    title = title,
    description = description,
    version = version,
    loaderLabel = loader?.displayName,
    iconFilePath = iconFilePath,
    enabled = enabled,
    providers = providers
)

@Immutable
enum class RemovalKind {
    Mod, ResourcePack, ShaderPack
}

@Immutable
data class PendingRemoval(
    val kind: RemovalKind,
    val fileName: String,
    val title: String,
)

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
