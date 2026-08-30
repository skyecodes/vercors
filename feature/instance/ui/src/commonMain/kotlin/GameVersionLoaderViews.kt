package app.vercors.feature.instance.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

@Composable
fun observeGameVersionListView(
    observeGameVersionList: ObserveGameVersionListUseCase,
): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
    produceAndCollectAsProgressiveState {
        observeGameVersionList().map { res -> res.map { it.toView() } }
    }

@Composable
fun observeAvailableLoadersView(
    observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    gameVersion: String?,
): State<ProgressiveOutcome<LoaderError, List<ModLoaderType>>> =
    produceAndCollectAsProgressiveState(gameVersion) {
        gameVersion?.let { observeAvailableLoaders(it) } ?: flowOf(ProgressiveOutcome.Incomplete())
    }

@Composable
fun observeLoaderVersionsView(
    observeLoaderVersions: ObserveLoaderVersionsUseCase,
    gameVersion: String?,
    loader: ModLoaderType?,
): State<ProgressiveOutcome<LoaderError, List<LoaderVersion>>> =
    produceAndCollectAsProgressiveState(gameVersion, loader) {
        if (gameVersion != null && loader != null) observeLoaderVersions(gameVersion, loader)
        else flowOf(ProgressiveOutcome.Incomplete())
    }
