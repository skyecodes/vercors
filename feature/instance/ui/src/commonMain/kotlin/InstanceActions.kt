package app.vercors.feature.instance.ui

import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.OpenInstanceFolderUseCase
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsTab
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
class InstanceActions(
    private val globalScope: CoroutineScope,
    private val launchInstance: LaunchInstanceUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val openInstanceFolder: OpenInstanceFolderUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
) {
    fun launch(instanceId: InstanceId, navigator: Navigator) {
        globalScope.launch {
            either { launchInstance(instanceId) }.onLeft { error ->
                when (error) {
                    is InstanceLaunchError.NoCompatibleJava, InstanceLaunchError.LoginRequired ->
                        navigator.goTo(
                            InstanceDetailsScreen(
                                instanceId,
                                initialTab = InstanceDetailsTab.Logs,
                                autoLaunch = true
                            )
                        )

                    else -> snackbarService.error { error.message }
                }
            }
        }
    }

    fun stop(instanceId: InstanceId) {
        globalScope.launch { stopInstance(instanceId) }
    }

    fun kill(instanceId: InstanceId) {
        globalScope.launch { killInstance(instanceId) }
    }

    fun openFolder(instanceId: InstanceId) {
        globalScope.launch {
            either { openInstanceFolder(instanceId, InstanceFolder.Root) }
                .onLeft { snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) } }
        }
    }

    fun delete(instanceId: InstanceId) {
        globalScope.launch {
            either { deleteInstance(instanceId) }.onLeft { error -> snackbarService.error { error.message } }
        }
    }
}
