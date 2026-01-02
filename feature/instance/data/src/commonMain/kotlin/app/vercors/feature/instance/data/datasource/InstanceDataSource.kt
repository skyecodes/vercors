package app.vercors.feature.instance.data.datasource

import app.vercors.feature.instance.data.dto.InstanceData
import app.vercors.shared.data.JsonFileListStoreFactory
import kotlinx.io.files.Path
import org.koin.core.annotation.Single

internal const val INSTANCES_DIR_NAME = "instances"

@Single
internal class InstanceDataSource(
    appPath: Path,
    jsonFileListStoreFactory: JsonFileListStoreFactory
) {
    private val instancesDir = Path(appPath, INSTANCES_DIR_NAME)
    private val store = jsonFileListStoreFactory("instances", instancesDir, InstanceData.serializer())

    val instancesState = store.state

    suspend fun saveToFile(value: InstanceData, id: String) = store.write(id, value)

    suspend fun delete(id: String) = store.delete(id)
}
