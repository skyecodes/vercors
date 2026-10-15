package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.export.InstanceExportError
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.instance.domain.repository.InstanceExportRepository
import app.vercors.feature.project.domain.model.FileHashes
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectFileMatch
import app.vercors.feature.project.domain.model.folderName
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull

private val logger = KotlinLogging.logger {}

private const val DISABLED_SUFFIX = ".disabled"

@Inject
class IdentifyInstalledContentUseCase(
    private val instanceContentRepository: InstanceContentRepository,
    private val instanceExportRepository: InstanceExportRepository,
    private val projectRepository: ProjectRepository,
) {
    suspend operator fun invoke(instanceId: InstanceId) {
        val unknown = ContentKind.entries.flatMap { kind -> unknownFiles(instanceId, kind) }
        if (unknown.isEmpty()) return
        val modrinthMatches = findMatches(MODRINTH_PLATFORM_ID, unknown.map { it.file })
        val unmatched = unknown.filter { it.file.sha1 !in modrinthMatches }
        val curseForgeMatches = findMatches(CURSEFORGE_PLATFORM_ID, unmatched.map { it.file })
        for (entry in unknown) {
            val sha1 = entry.file.sha1 ?: continue
            val match = modrinthMatches[sha1] ?: curseForgeMatches[sha1] ?: continue
            instanceContentRepository.recordOrigin(
                entry.kind,
                instanceId,
                entry.fileName,
                match.projectId.platform,
                match.projectId.id,
                match.file.id
            )
        }
    }

    private suspend fun unknownFiles(instanceId: InstanceId, kind: ContentKind): List<UnknownContent> {
        val installed = instanceContentRepository.observeInstalledContent(kind, instanceId).now().valueOrNull
        val unknownNames = installed.orEmpty().filter { it.providers.isEmpty() }.mapTo(HashSet()) { it.fileName }
        if (unknownNames.isEmpty()) return emptyList()
        val files = recover({
            instanceExportRepository.collectFiles(instanceId, setOf(kind.folderName), hashContent = true)
        }) { error: InstanceExportError ->
            logger.warn { "Couldn't hash ${kind.folderName} of $instanceId: $error" }
            emptyList()
        }
        return files.mapNotNull { file ->
            val fileName = file.relativePath.removePrefix("${kind.folderName}/").removeSuffix(DISABLED_SUFFIX)
            if (fileName in unknownNames && file.sha1 != null) UnknownContent(kind, fileName, file) else null
        }
    }

    private suspend fun findMatches(
        platformId: String,
        files: List<InstanceExportFile>
    ): Map<String, ProjectFileMatch> {
        val hashes = files.mapNotNull { file -> file.sha1?.let { FileHashes(it, file.curseForgeFingerprint) } }
            .distinctBy { it.sha1 }
        if (hashes.isEmpty()) return emptyMap()
        return recover({
            projectRepository.findFilesByHashes(platformId, hashes)
        }) { error: ProjectDetailsError ->
            logger.warn { "$platformId hash lookup failed during content identification: $error" }
            emptyList()
        }.associateBy { it.sha1 }
    }

    private data class UnknownContent(val kind: ContentKind, val fileName: String, val file: InstanceExportFile)
}
