package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import dev.zacsweers.metro.Inject

@Inject
class SetContentEnabledUseCase(
    private val instanceContentRepository: InstanceContentRepository
) {
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId, fileName: String, enabled: Boolean): Unit =
        instanceContentRepository.setEnabled(kind, instanceId, fileName, enabled)
}
