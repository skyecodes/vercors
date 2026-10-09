package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import app.vercors.feature.instance.domain.repository.PrismInstanceImportRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class ImportPrismInstanceFilesUseCase(
    private val prismInstanceImportRepository: PrismInstanceImportRepository
) {
    context(_: Raise<LocalImportError>)
    suspend operator fun invoke(
        sourcePath: String,
        instanceId: InstanceId,
        info: PrismInstanceInfo,
        onProgress: (Progress) -> Unit = {}
    ): String? = prismInstanceImportRepository.importGameFiles(sourcePath, instanceId, info, onProgress)
}
