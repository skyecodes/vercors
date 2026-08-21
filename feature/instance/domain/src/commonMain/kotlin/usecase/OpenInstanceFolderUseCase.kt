package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject

@Inject
class OpenInstanceFolderUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, folder: InstanceFolder): Boolean =
        instanceRepository.openInstanceFolder(instanceId, folder)
}
