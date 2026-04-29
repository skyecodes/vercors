package app.vercors.feature.instance.data.datasource

import app.vercors.core.filesystem.FileListStore
import app.vercors.core.filesystem.FileListStoreFactory
import app.vercors.feature.instance.data.dto.InstanceData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

internal const val INSTANCES_DIR_NAME = "instances"

@SingleIn(AppScope::class)
@Inject
class InstanceDataSource(
    appPath: Path,
    fileListStoreFactory: FileListStoreFactory
) : FileListStore<InstanceData> by fileListStoreFactory(
    directoryPath = Path(appPath, INSTANCES_DIR_NAME),
    serializer = InstanceData.serializer()
)
