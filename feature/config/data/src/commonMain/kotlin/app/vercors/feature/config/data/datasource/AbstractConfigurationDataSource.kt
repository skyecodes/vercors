package app.vercors.feature.config.data.datasource

import app.vercors.shared.data.JsonFileStateFactory
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer

abstract class AbstractConfigurationDataSource<T : Any>(
    configFileName: String,
    appPath: Path,
    serializer: KSerializer<T>,
    default: () -> T,
    jsonFileStateFactory: JsonFileStateFactory
) {
    private val configFile = Path(appPath, configFileName)
    private val jsonFileState = jsonFileStateFactory(configFile, serializer, default)

    val configState = jsonFileState.state

    suspend fun saveToFile(value: T) = jsonFileState.write(value)
}
