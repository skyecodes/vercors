package app.vercors.feature.theme.data.datasource

import app.vercors.core.filesystem.FileListStore
import app.vercors.core.filesystem.FileListStoreFactory
import app.vercors.feature.theme.data.dto.ThemeJsonData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

@SingleIn(AppScope::class)
@Inject
class CustomThemeDataSource(
    appPath: Path,
    fileListStoreFactory: FileListStoreFactory,
) : FileListStore<ThemeJsonData> by fileListStoreFactory(
    directoryPath = Path(appPath, "themes"),
    serializer = ThemeJsonData.serializer(),
)
