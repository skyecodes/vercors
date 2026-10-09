package app.vercors.feature.instance.data.content

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.FileWatcherService
import app.vercors.core.filesystem.HashedJsonCacheFactory
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

private const val RESOURCE_PACK_KIND = "resourcepack"

@SingleIn(AppScope::class)
@Inject
class InstanceResourcePackScannerService(
    appPaths: AppPaths,
    fileWatcherService: FileWatcherService,
    hashedJsonCacheFactory: HashedJsonCacheFactory,
    cryptographyService: CryptographyService,
    backgroundScope: CoroutineScope,
    json: Json
) : ContentDirScannerService(
    ContentDirScannerEngine(
        instanceDir = { appPaths.resourcePacksDir(it) },
        extension = "zip",
        metadataCache = hashedJsonCacheFactory(
            appPaths.metadataCache(RESOURCE_PACK_KIND),
            ContentFileMetadata.serializer()
        ),
        iconCacheDir = appPaths.iconCache(RESOURCE_PACK_KIND),
        parser = ResourcePackParser(json),
        fileWatcherService = fileWatcherService,
        cryptographyService = cryptographyService,
        backgroundScope = backgroundScope
    )
)
