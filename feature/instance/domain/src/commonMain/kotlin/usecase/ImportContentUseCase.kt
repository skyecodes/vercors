package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class ImportContentUseCase(
    private val instanceContentRepository: InstanceContentRepository
) {
    context(_: Raise<ImportError>)
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId, sourcePath: String): Unit =
        instanceContentRepository.import(kind, instanceId, sourcePath)
}
