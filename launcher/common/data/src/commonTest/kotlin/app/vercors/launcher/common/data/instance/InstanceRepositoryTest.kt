package app.vercors.launcher.common.data.instance

class InstanceRepositoryTest {
    /*private val baseDir = Path("testDir")

    @BeforeTest
    @AfterTest
    fun clearFiles() {
        SystemFileSystem.deleteRecursively(baseDir)
    }

    @Test
    fun `given initial state when creating instance then update instances StateFlow and create file`() = runTest {
        val commonConfigurationRepository = object : CommonConfigurationRepository {
            override val state: ResourceStateFlow<CommonConfiguration, Throwable> =
                MutableResourceStateFlow(Resource.Success(CommonConfiguration(baseDir = baseDir)))

            override suspend fun load() {}
            override suspend fun save(commonConfiguration: CommonConfiguration) {}
        }
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository: InstanceRepository =
            InstanceRepositoryImpl(Json, backgroundScope, commonConfigurationRepository, dispatcher)
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
    )*/
}