package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import dev.zacsweers.metro.Inject

@Inject
class ImportModUseCase(
    private val instanceModRepository: InstanceModRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, sourcePath: String): Boolean =
        instanceModRepository.importMod(instanceId, sourcePath)
}
