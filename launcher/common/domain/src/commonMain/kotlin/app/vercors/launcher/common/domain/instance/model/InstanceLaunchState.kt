package app.vercors.launcher.common.domain.instance.model

import app.vercors.launcher.common.domain.account.model.TokenValidationState
import kotlinx.coroutines.Job

sealed interface InstanceLaunchState {
    data object LoginRequired : InstanceLaunchState
    data class ValidatingToken(val state: TokenValidationState) : InstanceLaunchState
    data class SettingUp(val loginState: InstanceSetupState) : InstanceLaunchState
    data class Running(val job: Job) : InstanceLaunchState
    data class Error(val error: Throwable) : InstanceLaunchState
}

sealed interface InstanceSetupState {
    sealed interface Ongoing : InstanceSetupState {
        val progress: Float

        data class Client(override val progress: Float) : Ongoing
        data class Libraries(override val progress: Float) : Ongoing
        data class Assets(override val progress: Float) : Ongoing
        data class Content(override val progress: Float) : Ongoing
    }

    data object Success : InstanceSetupState
    data class Error(val error: Throwable) : InstanceSetupState
}
