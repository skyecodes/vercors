package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.domain.model.InstanceFolderError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class OpenInstanceFolderUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceFolderError>)
    suspend operator fun invoke(instanceId: InstanceId, folder: InstanceFolder): Unit =
        instanceRepository.openInstanceFolder(instanceId, folder)
}
