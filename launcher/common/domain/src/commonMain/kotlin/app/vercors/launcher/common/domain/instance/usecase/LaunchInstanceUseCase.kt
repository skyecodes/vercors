package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.instance.InstanceRepository
import org.koin.core.annotation.Factory

@Factory
class LaunchInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instanceId: String) {
        TODO("Not yet implemented")
    }
}