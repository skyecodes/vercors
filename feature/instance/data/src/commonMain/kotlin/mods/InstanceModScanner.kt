package app.vercors.feature.instance.data.mods

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.FileWatcherService
import app.vercors.core.filesystem.HashedJsonCacheFactory
import app.vercors.feature.instance.data.content.ContentDirScanner
import app.vercors.feature.instance.data.content.ContentDirScannerEngine
import app.vercors.feature.instance.data.content.ContentFileMetadata
import app.vercors.feature.instance.data.content.ParsedContentInfo
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

private const val MOD_KIND = "mod"

@SingleIn(AppScope::class)
@Inject
class InstanceModScanner(
    appPaths: AppPaths,
    fileWatcherService: FileWatcherService,
    hashedJsonCacheFactory: HashedJsonCacheFactory,
    cryptographyService: CryptographyService,
    backgroundScope: CoroutineScope,
    json: Json
) : ContentDirScanner(
    ContentDirScannerEngine(
        instanceDir = { appPaths.modsDir(it) },
        extension = "jar",
        metadataCache = hashedJsonCacheFactory(appPaths.metadataCache(MOD_KIND), ContentFileMetadata.serializer()),
        iconCacheDir = appPaths.iconCache(MOD_KIND),
        parser = { parseModJar(it, json).toContentInfo() },
        fileWatcherService = fileWatcherService,
        cryptographyService = cryptographyService,
        backgroundScope = backgroundScope
    )
)

private fun ParsedModInfo.toContentInfo(): ParsedContentInfo = ParsedContentInfo(
    title = title,
    description = description,
    version = version,
    loader = loader,
    iconEntryPath = iconEntryPath
)
