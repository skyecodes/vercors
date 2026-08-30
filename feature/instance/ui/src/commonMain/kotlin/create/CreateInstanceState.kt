package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
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
) {
    val canCreateInstance: Boolean = instanceName.length >= 3 && selectedGameVersion != null &&
            (selectedLoader == null || selectedLoaderVersion != null)
}

@Immutable
enum class InstanceType {
    Custom,
    Modpack,
    Import
}
