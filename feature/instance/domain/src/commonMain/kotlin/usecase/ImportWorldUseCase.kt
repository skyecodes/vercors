package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceWorldRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class ImportWorldUseCase(
    private val instanceWorldRepository: InstanceWorldRepository
) {
    context(_: Raise<ImportError>)
    suspend operator fun invoke(instanceId: InstanceId, sourcePath: String): Unit =
        instanceWorldRepository.importWorld(instanceId, sourcePath)
}
