package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.localimport.PrismImportResult
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import arrow.core.raise.Raise
import opensavvy.progress.Progress

interface PrismInstanceImportRepository {
    context(_: Raise<LocalImportError>)
    suspend fun parse(sourcePath: String): PrismInstanceInfo

    context(_: Raise<LocalImportError>)
    suspend fun importGameFiles(
        sourcePath: String,
        instanceId: String,
        info: PrismInstanceInfo,
        preserveUserState: Boolean = false,
        onProgress: (Progress) -> Unit = {}
    ): PrismImportResult
}
