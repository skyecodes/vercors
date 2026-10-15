package app.vercors.feature.instance.ui.install

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.UpdateInstanceJobUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.feature.project.domain.model.pack.LocalPack
import app.vercors.feature.project.domain.usecase.UpdateFromLocalPackUseCase
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class LocalPackUpdateLauncher(
    private val globalScope: CoroutineScope,
    private val updateFromLocalPack: UpdateFromLocalPackUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateInstanceJob: UpdateInstanceJobUseCase,
    private val snackbarService: SnackbarService,
) {
    fun launch(instanceId: InstanceId, instanceName: String, pack: LocalPack) {
        val job = globalScope.launch {
            val ok = either { updateFromLocalPack(instanceId, pack) }
                .onLeft { error -> snackbarService.error { error.message } }
                .isRight()
            updateInstanceStatus(instanceId, if (ok) InstanceStatus.Idle else InstanceStatus.Failed.InstallFailed)
            notifyModpackInstallResult(instanceName, ok)
        }
        updateInstanceJob(instanceId, job)
        // A cancelled update leaves a half-replaced game dir
        job.invokeOnCompletion { error ->
            updateInstanceJob(instanceId, null)
            if (error != null) updateInstanceStatus(instanceId, InstanceStatus.Failed.InstallFailed)
        }
    }
}
