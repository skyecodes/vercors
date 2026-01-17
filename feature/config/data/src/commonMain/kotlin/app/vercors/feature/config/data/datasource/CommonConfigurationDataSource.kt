package app.vercors.feature.config.data.datasource

import app.vercors.feature.config.data.dto.CommonConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.domain.model.CommonConfiguration
import app.vercors.shared.data.JsonFileStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
internal class CommonConfigurationDataSource(
    appPath: Path,
    jsonFileStoreFactory: JsonFileStore.Factory<CommonConfigurationData>,
) : AbstractConfigurationDataSource<CommonConfigurationData>(
    storeName = "config-common",
    configFileName = "config-common.json",
    appPath = appPath,
    serializer = CommonConfigurationData.serializer(),
    default = { CommonConfiguration().toData() },
    jsonFileStoreFactory = jsonFileStoreFactory
)
