package app.vercors.feature.config.data.datasource

import app.vercors.feature.config.data.dto.CommonConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.domain.model.CommonConfiguration
import app.vercors.shared.data.JsonFileStateFactory
import kotlinx.io.files.Path
import org.koin.core.annotation.Single

@Single
internal class CommonConfigurationDataSource(
    appPath: Path,
    jsonFileStateFactory: JsonFileStateFactory,
) : AbstractConfigurationDataSource<CommonConfigurationData>(
    configFileName = "config-common.json",
    appPath = appPath,
    serializer = CommonConfigurationData.serializer(),
    default = { CommonConfiguration().toData() },
    jsonFileStateFactory = jsonFileStateFactory
)
