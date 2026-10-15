package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.pack.LocalPack
import opensavvy.state.progressive.ProgressiveOutcome

@Immutable
data class CreateInstanceState(
    val selectedInstanceType: InstanceType,
    val instanceName: String,
    val selectedGameVersion: GameVersionView?,
    val gameVersionList: ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>,
    val selectedLoader: ModLoaderType?,
    val loaderList: ProgressiveOutcome<LoaderError, List<ModLoaderType>>,
    val selectedLoaderVersion: LoaderVersion?,
    val loaderVersionList: ProgressiveOutcome<LoaderError, List<LoaderVersion>>,
    val importParsing: Boolean,
    val importPreview: LocalPack?,
    val importTargets: List<ImportTargetView>,
    val selectedImportTargetId: InstanceId?,
    val importInstanceName: String,
    val importing: Boolean,
) {
    val canCreateInstance: Boolean = instanceName.length >= 3 && selectedGameVersion != null &&
            (selectedLoader == null || selectedLoaderVersion != null)
    val isImportUpdate: Boolean = selectedImportTargetId != null
    val canConfirmImport: Boolean = importPreview != null && !importing &&
            (isImportUpdate || importInstanceName.length >= 3)
}

@Immutable
data class ImportTargetView(
    val id: InstanceId,
    val name: String,
)

@Immutable
enum class InstanceType {
    Custom,
    Modpack,
    Import
}
