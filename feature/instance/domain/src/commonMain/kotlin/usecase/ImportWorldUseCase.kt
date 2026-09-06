package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceWorldRepository
import dev.zacsweers.metro.Inject

@Inject
class ImportWorldUseCase(
    private val instanceWorldRepository: InstanceWorldRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, sourcePath: String): Boolean =
        instanceWorldRepository.importWorld(instanceId, sourcePath)
}
