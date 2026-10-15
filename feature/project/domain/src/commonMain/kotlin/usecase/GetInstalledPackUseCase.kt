package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.pack.InstalledPack
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import dev.zacsweers.metro.Inject

@Inject
class GetInstalledPackUseCase(
    private val installedPackRepository: InstalledPackRepository,
) {
    suspend operator fun invoke(instanceId: InstanceId): InstalledPack? = installedPackRepository.get(instanceId)
}
