package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import dev.zacsweers.metro.Inject

@Inject
class RefreshInstalledContentUseCase(
    private val instanceContentRepository: InstanceContentRepository
) {
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId): Unit =
        instanceContentRepository.refresh(kind, instanceId)
}
