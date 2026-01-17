package app.vercors.feature.config.data.datasource

import app.vercors.shared.data.JsonFileStore
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer

abstract class AbstractConfigurationDataSource<T : Any>(
    storeName: String,
    configFileName: String,
    appPath: Path,
    serializer: KSerializer<T>,
    default: () -> T,
    jsonFileStoreFactory: JsonFileStore.Factory<T>
) {
    private val configFile = Path(appPath, configFileName)
    private val store = jsonFileStoreFactory.create(storeName, configFile, serializer, default)

    val configState = store.state

    suspend fun saveToFile(value: T) = store.write(value)
}
