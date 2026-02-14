package app.vercors.feature.config.data.datasource

import app.vercors.core.filesystem.FileStore
import app.vercors.core.filesystem.FileStoreFactory
import app.vercors.feature.config.data.dto.ConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.domain.model.Configuration
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

@SingleIn(AppScope::class)
@Inject
class ConfigurationDataSource(
    appPath: Path,
    fileStoreFactory: FileStoreFactory
) : FileStore<ConfigurationData> by fileStoreFactory(
    filePath = Path(appPath, "config.json"),
    serializer = ConfigurationData.serializer(),
    defaultProvider = { Configuration.Default.toData() }
)
