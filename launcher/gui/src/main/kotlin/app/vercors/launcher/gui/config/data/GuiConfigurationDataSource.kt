package app.vercors.launcher.gui.config.data

import app.vercors.launcher.common.data.AppPath
import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.config.AbstractConfigurationDataSource
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
internal class GuiConfigurationDataSource(
    coroutineScope: CoroutineScope,
    @AppPath appPath: Path,
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