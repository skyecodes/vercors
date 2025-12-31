package app.vercors.feature.config.gui.data.datasource

import app.vercors.feature.config.data.datasource.AbstractConfigurationDataSource
import app.vercors.feature.config.gui.data.dto.GuiConfigurationData
import app.vercors.shared.data.IODispatcher
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
internal class GuiConfigurationDataSource(
    coroutineScope: CoroutineScope,
    appPath: Path,
    @IODispatcher ioDispatcher: CoroutineDispatcher,
    json: Json,
    watcher: KfsDirectoryWatcher
) : AbstractConfigurationDataSource<GuiConfigurationData>(
    configFileName = "config-gui.json",
    coroutineScope = coroutineScope,
    appPath = appPath,
    ioDispatcher = ioDispatcher,
    json = json,
    watcher = watcher,
    serializer = GuiConfigurationData.serializer()
)
