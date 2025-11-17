package app.vercors.launcher.common.data.config

import app.cash.turbine.test
import app.vercors.launcher.common.data.deleteRecursively
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlin.test.*

class CommonConfigurationDataSourceTest {
    val baseDir = Path("testDir")
    lateinit var ioDispatcher: CoroutineDispatcher
    lateinit var json: Json
    lateinit var watcher: KfsDirectoryWatcher
    internal lateinit var dataSource: CommonConfigurationDataSource

    @BeforeTest
    @AfterTest
    fun clearFiles() {
        SystemFileSystem.deleteRecursively(baseDir)
    }

    @Test
    fun `given no config file when loadFromFile then return null`() = runTest {
        init()

        assertNull(dataSource.loadFromFile())
    }

    @Test
    fun `given existing config file when loadFromFile then return data`() = runTest {
        init()
        val data = CommonConfigurationData("test")

        writeToFile(data)

        assertEquals(data, dataSource.loadFromFile())
    }

    @Test
    fun `given no config file when saveToFile then creates file`() = runTest {
        init()
        val data = CommonConfigurationData("test")
        dataSource.saveToFile(data)

        val fileData = readFromFile()

        assertEquals(data, fileData)
    }

    @Test
    fun `given existing config file when saveToFile then updates file`() = runTest {
        init()
        val data = CommonConfigurationData("test")
        writeToFile(data)

        val newData = CommonConfigurationData("test2")
        dataSource.saveToFile(newData)

        val fileData = readFromFile()
        assertEquals(newData, fileData)
    }

    @Test
    fun `given no config file when saveToFile then send update`() = runTest {
        init()
        watcher.onStartFlow.test { val res = awaitItem() }
        val data = CommonConfigurationData("test")

        dataSource.updates.test {
            dataSource.saveToFile(data)

            assertEquals(data, awaitItem())
        }
    }

    @Test
    fun `given existing config file when delete file then send update`() = runTest {
        init()
        val data = CommonConfigurationData("test")
        writeToFile(data)
        watcher.onStartFlow.test { val res = awaitItem() }

        dataSource.updates.test {
            SystemFileSystem.delete(dataSource.configFile)

            assertNull(awaitItem())
        }
    }

    private suspend fun readFromFile(): CommonConfigurationData = withContext(ioDispatcher) {
        SystemFileSystem.source(dataSource.configFile).buffered().use {
            json.decodeFromSource<CommonConfigurationData>(it)
        }
    }

    private suspend fun writeToFile(data: CommonConfigurationData) = withContext(ioDispatcher) {
        SystemFileSystem.sink(dataSource.configFile).buffered().use {
            json.encodeToSink(data, it)
        }
    }

    private fun TestScope.init() {
        SystemFileSystem.createDirectories(baseDir)
        ioDispatcher = StandardTestDispatcher(testScheduler)
        json = Json
        watcher = KfsDirectoryWatcher(backgroundScope, ioDispatcher)
        dataSource = CommonConfigurationDataSource(backgroundScope, baseDir, ioDispatcher, json, watcher)
    }
}