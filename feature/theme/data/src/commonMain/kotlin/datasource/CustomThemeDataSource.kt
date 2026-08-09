package app.vercors.feature.theme.data.datasource

import app.vercors.core.filesystem.FileListStore
import app.vercors.core.filesystem.FileListStoreFactory
import app.vercors.feature.theme.data.dto.ThemeDefinitionData
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class CustomThemeDataSource(
    appPaths: AppPaths,
    fileListStoreFactory: FileListStoreFactory,
) : FileListStore<ThemeDefinitionData> by fileListStoreFactory(
    directoryPath = appPaths.themes,
    serializer = ThemeDefinitionData.serializer(),
)
