package app.vercors.feature.project.ui.install

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.DownloadProgress
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.project.domain.model.*
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Instant

@Immutable
data class InstallProgressView(
    val fraction: Float = 0f,
    val download: DownloadProgress? = null,
)

@Immutable
data class InstallProjectState(
    val projectType: ProjectType,
    val instances: ProgressiveOutcome<Nothing, List<InstallableInstanceView>>,
    val selectedInstanceId: InstanceId?,
    val creatingNewInstance: Boolean,
    val newInstanceName: String,
    val project: ProgressiveOutcome<ProjectDetailsError, Project>,
    val newInstanceGameVersionList: List<GameVersionView>,
    val selectedNewGameVersion: GameVersionView?,
    val newInstanceLoaderList: List<ModLoaderType>,
    val selectedNewLoader: ModLoaderType?,
    val newInstanceLoaderVersionList: ProgressiveOutcome<LoaderError, List<LoaderVersion>>,
    val selectedNewLoaderVersion: LoaderVersion?,
    val files: ProgressiveOutcome<ProjectDetailsError, List<ProjectFileView>>,
    val selectedFileId: String?,
    val dependencies: List<DependencyFileView>,
    val selectedDependencyIds: Set<ProjectId>,
    val resolvingDependencies: Boolean,
    val installing: Boolean,
    val installProgress: InstallProgressView = InstallProgressView(),
    val manualDownloadItems: List<ManualDownloadItem> = emptyList(),
    val completedInstanceId: InstanceId? = null,
) {
    val instanceList = instances.valueOrNull ?: emptyList()
    val selectedInstance = instanceList.find { it.id == selectedInstanceId }
    val instanceSelectorItems: List<InstanceSelectorItem> =
        instanceList.map { InstanceSelectorItem.Existing(it) } + InstanceSelectorItem.CreateNew
    val selectedInstanceItem: InstanceSelectorItem? = when {
        creatingNewInstance -> InstanceSelectorItem.CreateNew
        selectedInstance != null -> InstanceSelectorItem.Existing(selectedInstance)
        else -> null
    }
    val fileList = files.valueOrNull ?: emptyList()
    val selectedFile = fileList.find { it.id == selectedFileId }
    val canCreateNewInstance = newInstanceName.length >= 3 && selectedNewGameVersion != null &&
            (!projectType.requiresLoader || (selectedNewLoader != null && selectedNewLoaderVersion != null))
    val canInstall = (selectedInstance != null || (creatingNewInstance && canCreateNewInstance)) &&
            selectedFile != null && !resolvingDependencies && !installing
}

sealed interface InstanceSelectorItem {
    data class Existing(val instance: InstallableInstanceView) : InstanceSelectorItem
    data object CreateNew : InstanceSelectorItem
}

@Immutable
data class InstallableInstanceView(
    val id: InstanceId,
    val name: String,
    val gameVersion: String,
    val loaderType: ModLoaderType?,
    val loaderVersion: String?,
    val updatedAt: Instant,
)

@Immutable
data class ProjectFileView(
    val id: String,
    val displayName: String,
    val fileName: String,
    val gameVersions: List<String>,
    val releaseChannel: ProjectReleaseChannel,
    val size: Long,
    val sha1: String?,
    val md5: String?,
    val publishedAt: Instant,
    val dependencies: List<ProjectId>,
    val manualDownloadUrl: String?,
    val loaders: List<ModLoaderType>,
)

@Immutable
data class DependencyFileView(
    val projectId: ProjectId,
    val name: String,
    // A dependency's own type decides its target folder, not the parent's
    val type: ProjectType,
    val file: ProjectFileView,
)

internal fun Instance.toInstallableInstanceViewOrNull(
    projectType: ProjectType,
    compatibleLoaders: List<ModLoaderType>
): InstallableInstanceView? {
    if (projectType.requiresLoader) {
        val loader = modLoader ?: return null
        if (loader.type !in compatibleLoaders) return null
        return InstallableInstanceView(
            id = id,
            name = name,
            gameVersion = gameVersion,
            loaderType = loader.type,
            loaderVersion = loader.version,
            updatedAt = updatedAt
        )
    }
    return InstallableInstanceView(
        id = id,
        name = name,
        gameVersion = gameVersion,
        loaderType = modLoader?.type,
        loaderVersion = modLoader?.version,
        updatedAt = updatedAt
    )
}

internal fun ProjectFile.toView(): ProjectFileView = ProjectFileView(
    id = id,
    displayName = displayName,
    fileName = fileName,
    gameVersions = gameVersions,
    releaseChannel = releaseChannel,
    size = size,
    sha1 = sha1,
    md5 = md5,
    publishedAt = publishedAt,
    dependencies = dependencies,
    manualDownloadUrl = manualDownloadUrl,
    loaders = loaders
)
