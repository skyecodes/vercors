package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import dev.zacsweers.metro.Inject

@Inject
class ImportResourcePackUseCase(
    private val instanceResourcePackRepository: InstanceResourcePackRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, sourcePath: String): Boolean =
        instanceResourcePackRepository.importResourcePack(instanceId, sourcePath)
}
