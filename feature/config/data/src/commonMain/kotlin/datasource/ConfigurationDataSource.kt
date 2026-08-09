package app.vercors.feature.config.data.datasource

import app.vercors.core.filesystem.FileStore
import app.vercors.core.filesystem.FileStoreFactory
import app.vercors.feature.config.data.dto.ConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class ConfigurationDataSource(
    appPaths: AppPaths,
    fileStoreFactory: FileStoreFactory
) : FileStore<ConfigurationData> by fileStoreFactory(
    filePath = appPaths.configFile,
    serializer = ConfigurationData.serializer(),
    defaultProvider = { Configuration.Default.toData() }
)
