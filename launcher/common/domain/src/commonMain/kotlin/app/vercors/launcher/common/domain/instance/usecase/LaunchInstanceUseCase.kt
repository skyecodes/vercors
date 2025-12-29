package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.account.AccountRepository
import app.vercors.launcher.common.domain.account.usecase.ValidateTokenUseCase
import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.instance.LauncherService
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.common.domain.instance.model.InstanceLaunchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory

@Factory
class LaunchInstanceUseCase(
    private val instanceRepository: InstanceRepository,
    private val accountRepository: AccountRepository,
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val launcherService: LauncherService
) {
    operator fun invoke(instanceId: InstanceId): Flow<InstanceLaunchState> = flow {
        val instance = instanceRepository.findInstance(instanceId)
        val account = accountRepository.getSelectedAccount()
        if (account == null) {
            emit(InstanceLaunchState.LoginRequired)
            return@flow
        }
        validateTokenUseCase(account).collect()
        launcherService.launch(instance, account, emptyList()).collect(this)
    }
}
