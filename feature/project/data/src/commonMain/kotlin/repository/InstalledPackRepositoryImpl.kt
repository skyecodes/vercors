package app.vercors.feature.project.data.repository

import app.vercors.core.filesystem.*
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.data.dto.InstalledPackData
import app.vercors.feature.project.data.toData
import app.vercors.feature.project.data.toDomain
import app.vercors.feature.project.domain.model.pack.InstalledPack
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstalledPackRepositoryImpl(
    private val json: Json,
    private val appPaths: AppPaths,
) : InstalledPackRepository {
    override suspend fun get(instanceId: InstanceId): InstalledPack? {
        val file = appPaths.installedPackFile(instanceId)
        if (!file.exists()) return null
        return try {
            file.readJson(InstalledPackData.serializer(), json).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Couldn't read the installed pack of $instanceId" }
            null
        }
    }

    // Written to a sibling tmp file: a truncated record would make the next update keep every old file
    override suspend fun save(instanceId: InstanceId, pack: InstalledPack) {
        val file = appPaths.installedPackFile(instanceId)
        val tmp = Path("$file.tmp")
        tmp.writeJson(InstalledPackData.serializer(), pack.toData(), json)
        tmp.moveTo(file)
    }

    override suspend fun deleteGameFile(instanceId: InstanceId, path: String) {
        val segments = path.split('/').filter { it.isNotBlank() }
        if (segments.isEmpty() || ".." in segments) return
        Path(appPaths.gameDir(instanceId), *segments.toTypedArray()).delete(mustExist = false)
    }
}
