package app.vercors.feature.instance.ui.details.options

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import kotlin.jvm.JvmInline

@Immutable
data class InstanceOptionsState(
    val editedInstance: InstanceEditState,
    val gameVersionList: List<GameVersionView>,
    val loaderList: List<ModLoaderType>,
    val loaderVersionList: List<LoaderVersion>,
    val availableJavaInstallations: List<JavaInstallation>,
    val totalMemoryMb: Int,
    val showNativeLibraryOptions: Boolean,
    val detectedGlfwPath: String?,
    val detectedOpenAlPath: String?,
    val globalJvmMemoryOptions: JvmMemoryOptions,
    val globalNativeLibraryOptions: NativeLibraryOptions,
    val inheritedJavaInstallation: JavaInstallation?,
    val requiredJavaMajorVersion: Int?,
    val globalCheckForUpdates: Boolean,
    val updateCheck: UpdateCheckView,
    private val persistedInstance: InstanceView?,
) {
    val isNameEdited = persistedInstance?.let {
        editedInstance.name != null && editedInstance.name != it.name
    } ?: false
    val isGameVersionEdited = persistedInstance?.let {
        editedInstance.gameVersion != null && editedInstance.gameVersion != it.gameVersion
    } ?: false
    val isLoaderTypeEdited = persistedInstance?.let {
        val current: ModLoaderTypeEdit = it.loaderType?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla
        editedInstance.modLoaderType != null && editedInstance.modLoaderType != current
    } ?: false
    val isLoaderVersionEdited = persistedInstance?.let {
        editedInstance.modLoaderVersion != null && editedInstance.modLoaderVersion != it.loaderVersion
    } ?: false
    val isModLoaderEdited = isLoaderTypeEdited || isLoaderVersionEdited
    val isJavaOverrideEdited = persistedInstance?.let {
        val current: JavaOverrideEdit =
            it.javaInstallationId?.let(JavaOverrideEdit::Specific) ?: JavaOverrideEdit.UseDefault
        editedInstance.javaOverride != null && editedInstance.javaOverride != current
    } ?: false
    private val effectiveEditedJvmMemoryOptions =
        editedInstance.jvmMemoryOptionsEdit?.withFallback(globalJvmMemoryOptions)
    private val effectivePersistedJvmMemoryOptions =
        persistedInstance?.jvmMemoryOptions?.withFallback(globalJvmMemoryOptions)
    val isMaxMemoryEdited = effectiveEditedJvmMemoryOptions != null && effectivePersistedJvmMemoryOptions != null &&
            (effectiveEditedJvmMemoryOptions.maxMemoryEnabled != effectivePersistedJvmMemoryOptions.maxMemoryEnabled ||
                    effectiveEditedJvmMemoryOptions.maxMemoryMb != effectivePersistedJvmMemoryOptions.maxMemoryMb)
    val isMinMemoryEdited = effectiveEditedJvmMemoryOptions != null && effectivePersistedJvmMemoryOptions != null &&
            (effectiveEditedJvmMemoryOptions.minMemoryEnabled != effectivePersistedJvmMemoryOptions.minMemoryEnabled ||
                    effectiveEditedJvmMemoryOptions.minMemoryMb != effectivePersistedJvmMemoryOptions.minMemoryMb)
    val isExtraJvmArgsEdited = effectiveEditedJvmMemoryOptions != null && effectivePersistedJvmMemoryOptions != null &&
            effectiveEditedJvmMemoryOptions.extraArgs != effectivePersistedJvmMemoryOptions.extraArgs
    val isJvmMemoryOptionsEdited = isMaxMemoryEdited || isMinMemoryEdited || isExtraJvmArgsEdited
    private val effectiveEditedNativeLibraryOptions =
        editedInstance.nativeLibraryOptionsEdit?.withFallback(globalNativeLibraryOptions)
    private val effectivePersistedNativeLibraryOptions =
        persistedInstance?.nativeLibraryOptions?.withFallback(globalNativeLibraryOptions)
    val isGlfwEdited = effectiveEditedNativeLibraryOptions != null && effectivePersistedNativeLibraryOptions != null &&
            (effectiveEditedNativeLibraryOptions.useSystemGlfw != effectivePersistedNativeLibraryOptions.useSystemGlfw ||
                    effectiveEditedNativeLibraryOptions.glfwPath != effectivePersistedNativeLibraryOptions.glfwPath)
    val isOpenAlEdited =
        effectiveEditedNativeLibraryOptions != null && effectivePersistedNativeLibraryOptions != null &&
                (effectiveEditedNativeLibraryOptions.useSystemOpenAl != effectivePersistedNativeLibraryOptions.useSystemOpenAl ||
                        effectiveEditedNativeLibraryOptions.openAlPath != effectivePersistedNativeLibraryOptions.openAlPath)
    val isNativeLibraryOptionsEdited = isGlfwEdited || isOpenAlEdited
    val checkForUpdatesOverride: Boolean? = editedInstance.checkForUpdatesEdit
        .let { edit -> if (edit != null) edit.enabled else persistedInstance?.checkForUpdates }
    val isCheckForUpdatesEdited = persistedInstance != null && editedInstance.checkForUpdatesEdit != null &&
            editedInstance.checkForUpdatesEdit.enabled != persistedInstance.checkForUpdates
    val isInstanceEdited: Boolean =
        isNameEdited || isGameVersionEdited || isModLoaderEdited || isJavaOverrideEdited ||
                isJvmMemoryOptionsEdited || isNativeLibraryOptionsEdited || isCheckForUpdatesEdited
}

@Immutable
data class InstanceEditState(
    val name: String? = null,
    val gameVersion: String? = null,
    val modLoaderType: ModLoaderTypeEdit? = null,
    val modLoaderVersion: String? = null,
    val javaOverride: JavaOverrideEdit? = null,
    val jvmMemoryOptionsEdit: JvmMemoryOptions? = null,
    val nativeLibraryOptionsEdit: NativeLibraryOptions? = null,
    val checkForUpdatesEdit: CheckForUpdatesEdit? = null,
)

// enabled = null means the instance inherits the global setting
@Immutable
@JvmInline
value class CheckForUpdatesEdit(val enabled: Boolean?)

@Immutable
data class UpdateCheckView(
    val isModpack: Boolean,
    val installedModpackVersion: String?,
    val checking: Boolean,
    val identifyUnknownFiles: Boolean,
    val checked: Boolean,
    val modpackUpdateName: String?,
    val contentUpdateCount: Int,
)

@Immutable
sealed interface ModLoaderTypeEdit {
    data object Vanilla : ModLoaderTypeEdit

    @JvmInline
    value class Loader(val type: ModLoaderType) : ModLoaderTypeEdit
}

@Immutable
sealed interface JavaOverrideEdit {
    data object UseDefault : JavaOverrideEdit

    @JvmInline
    value class Specific(val installationId: String) : JavaOverrideEdit
}
