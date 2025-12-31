package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.account.domain.repository.AccountRepository
import app.vercors.feature.account.domain.usecase.ValidateTokenUseCase
import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.launcher.domain.model.InstanceLaunchState
import app.vercors.feature.launcher.domain.service.LauncherService
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
