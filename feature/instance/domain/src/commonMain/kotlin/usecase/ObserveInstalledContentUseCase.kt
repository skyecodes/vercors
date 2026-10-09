package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstalledContentUseCase(
    private val instanceContentRepository: InstanceContentRepository
) {
    operator fun invoke(
        kind: ContentKind,
        instanceId: InstanceId
    ): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        instanceContentRepository.observeInstalledContent(kind, instanceId)
}
