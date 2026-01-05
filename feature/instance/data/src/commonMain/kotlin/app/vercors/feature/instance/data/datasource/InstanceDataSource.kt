package app.vercors.feature.instance.data.datasource

import app.vercors.feature.instance.data.dto.InstanceData
import app.vercors.shared.data.JsonFileListStoreFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

internal const val INSTANCES_DIR_NAME = "instances"

@SingleIn(AppScope::class)
@Inject
class InstanceDataSource(
    appPath: Path,
    jsonFileListStoreFactory: JsonFileListStoreFactory
) {
    private val instancesDir = Path(appPath, INSTANCES_DIR_NAME)
    private val store = jsonFileListStoreFactory("instances", instancesDir, InstanceData.serializer())

    val instancesState = store.state

    suspend fun saveToFile(value: InstanceData, id: String) = store.write(id, value)

    suspend fun delete(id: String) = store.delete(id)
}
