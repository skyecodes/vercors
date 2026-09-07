package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import arrow.core.raise.Raise

interface PrismInstanceImportRepository {
    context(_: Raise<LocalImportError>)
    suspend fun parse(sourcePath: String): PrismInstanceInfo

    context(_: Raise<LocalImportError>)
    suspend fun importGameFiles(
        sourcePath: String,
        instanceId: String,
        info: PrismInstanceInfo,
        onProgress: (Float?) -> Unit = {}
    ): String?
}
