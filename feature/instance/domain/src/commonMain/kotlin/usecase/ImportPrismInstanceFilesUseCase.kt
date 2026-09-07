package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import app.vercors.feature.instance.domain.repository.PrismInstanceImportRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class ImportPrismInstanceFilesUseCase(
    private val repository: PrismInstanceImportRepository
) {
    context(_: Raise<LocalImportError>)
    suspend operator fun invoke(
        sourcePath: String,
        instanceId: InstanceId,
        info: PrismInstanceInfo,
        onProgress: (Float?) -> Unit = {}
    ): String? = repository.importGameFiles(sourcePath, instanceId, info, onProgress)
}
