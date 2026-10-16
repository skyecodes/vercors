package app.vercors.feature.launcher.data.launch

import app.vercors.shared.data.toZipEntryName
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.extractTo
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

private val logger = KotlinLogging.logger {}

@Inject
class NativesExtractor {
    suspend fun extract(natives: List<DependencyFile.Native>): Unit = coroutineScope {
        if (natives.isEmpty()) return@coroutineScope
        logger.info { "Extracting natives..." }
        val extractDispatcher = Dispatchers.IO + SupervisorJob()
        natives.map { file ->
            launch(extractDispatcher) {
                logger.trace { "Extracting ${file.path} to ${file.extractDest}..." }
                Zip.open(file.path).use { zip ->
                    zip.extractTo(file.extractDest)
                    zip.forEachEntry { entry ->
                        val entryPath = entry.path.toZipEntryName()
                        if (file.extractExclude?.any { entryPath.startsWith(it) } ?: false) {
                            SystemFileSystem.delete(Path(file.extractDest, entryPath))
                        }
                    }
                }
                logger.trace { "Extracted ${file.path} to ${file.extractDest}" }
            }
        }.joinAll()
        logger.info { "Natives extracted" }
    }
}
