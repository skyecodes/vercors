package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.data.deleteRecursively
import app.vercors.launcher.common.domain.instance.Instance
import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.settings.GlobalSettings
import app.vercors.launcher.common.domain.settings.SettingsRepository
import app.vercors.shared.domain.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlin.test.*

class InstanceRepositoryTest {
    private val baseDir = Path("testDir")

    @BeforeTest
    @AfterTest
    fun clearFiles() {
        SystemFileSystem.deleteRecursively(baseDir)
    }

    @Test
    fun `given initial state when creating instance then update instances StateFlow and create file`() = runTest {
        val settingsRepository = object : SettingsRepository {
            override val globalSettings: ResourceStateFlow<GlobalSettings, Throwable> =
                MutableResourceStateFlow(Resource.Success(GlobalSettings(baseDir = baseDir)))

            override suspend fun loadGlobalSettings() {}
            override suspend fun updateGlobalSettings(settings: GlobalSettings) {}
        }
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository: InstanceRepository =
            InstanceRepositoryImpl(Json, backgroundScope, settingsRepository, dispatcher)
        lateinit var instances: List<Instance>
        launch { repository.instances.collectValues { instances = it } }
        testScheduler.advanceUntilIdle()

        // Initial state: Success with empty list
        assertEquals(0, instances.size)

        var instance = Instance(name = "Test Instance", gameVersion = "1.0.0")
        repository.saveInstance(instance)
        var data = loadInstanceFromFile(instance)
        testScheduler.advanceUntilIdle()

        instances = repository.instances.awaitValueOrThrow()
        assertEquals(1, instances.size)
        assertContains(instances, instance)
        assertEquals(instance, data.toDomain(instance.id))

        instance = instance.copy(name = "Test Instance Update", gameVersion = "1.1.0")
        repository.saveInstance(instance)
        data = loadInstanceFromFile(instance)
        testScheduler.advanceUntilIdle()

        instances = repository.instances.awaitValueOrThrow()
        assertEquals(1, instances.size)
        assertContains(instances, instance)
        assertEquals(instance, data.toDomain(instance.id))

        val newInstance = Instance(name = "New Test Instance", gameVersion = "2.0.0")
        repository.saveInstance(newInstance)
        val newData = loadInstanceFromFile(newInstance)
        testScheduler.advanceUntilIdle()

        instances = repository.instances.awaitValueOrThrow()
        assertEquals(2, instances.size)
        assertContains(instances, newInstance)
        assertEquals(newInstance, newData.toDomain(newInstance.id))

        repository.deleteInstance(instance)
        testScheduler.advanceUntilIdle()

        instances = repository.instances.awaitValueOrThrow()
        assertEquals(1, instances.size)
        assertContains(instances, newInstance)
        assertFalse(instances.contains(instance))
    }

    private fun loadInstanceFromFile(instance: Instance): InstanceData = Json.decodeFromSource<InstanceData>(
        SystemFileSystem.source(Path(baseDir, INSTANCES_DIR_NAME, instance.id, INSTANCE_FILE_NAME)).buffered()
    )
}