package app.vercors.feature.config.gui.data.datasource

import app.vercors.feature.config.data.datasource.AbstractConfigurationDataSource
import app.vercors.feature.config.gui.data.dto.GuiConfigurationData
import app.vercors.feature.config.gui.data.dto.toData
import app.vercors.feature.config.gui.domain.model.GuiConfiguration
import app.vercors.shared.data.JsonFileStoreFactory
import kotlinx.io.files.Path
import org.koin.core.annotation.Single

@Single
internal class GuiConfigurationDataSource(
    appPath: Path,
    jsonFileStoreFactory: JsonFileStoreFactory,
) : AbstractConfigurationDataSource<GuiConfigurationData>(
    storeName = "config-gui",
    configFileName = "config-gui.json",
    appPath = appPath,
    serializer = GuiConfigurationData.serializer(),
    default = { GuiConfiguration().toData() },
    jsonFileStoreFactory = jsonFileStoreFactory
)
