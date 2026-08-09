package app.vercors.feature.instance.data.datasource

import app.vercors.core.filesystem.FileListStore
import app.vercors.core.filesystem.FileListStoreFactory
import app.vercors.feature.instance.data.dto.InstanceData
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class InstanceDataSource(
    appPaths: AppPaths,
    fileListStoreFactory: FileListStoreFactory
) : FileListStore<InstanceData> by fileListStoreFactory(
    directoryPath = appPaths.instanceStore,
    serializer = InstanceData.serializer()
)
