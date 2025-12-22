package app.vercors.shared.domain

import app.cash.turbine.test
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResourceStateTest {
    data class TestData(val testValue: String)

    private val testData = TestData("test")

    @Test
    fun `given resource not collected when get value should return Unloaded`() = runTest {
        val flow = MutableResourceStateFlow<TestData>()
        assertEquals(Resource.Unloaded, flow.value)
    }

    @Test
    fun `given resource initialized with value when get value should return Loaded`() = runTest {
        val flow = MutableResourceStateFlow(testData)
        assertEquals(Resource.Success(testData), flow.value)
    }

    @Test
    fun `given resource loading when get value should return Loading`() = runTest {
        val flow = MutableResourceStateFlow<TestData>()
        backgroundScope.launch {
            flow.load(Mutex()) {
                delay(200)
                testData
            }
        }

        testScheduler.advanceTimeBy(100)
        assertEquals(Resource.Loading, flow.value)
    }

    @Test
    fun `given resource loaded when get value should return Loaded`() = runTest {
        val flow = MutableResourceStateFlow<TestData>()
        backgroundScope.launch {
            flow.load(Mutex()) {
                delay(200)
                testData
            }
        }

        testScheduler.advanceTimeBy(300)
        assertEquals(Resource.Success(testData), flow.value)
    }

    @Test
    fun `given resource error when get value should return Error`() = runTest {
        val testException = Exception("Test error")
        val flow = MutableResourceStateFlow<TestData>()
        backgroundScope.launch {
            flow.load(Mutex()) {
                delay(200)
                throw testException
            }
        }

        testScheduler.advanceTimeBy(300)
        assertEquals(Resource.Error(testException), flow.value)
    }

    @Test
    fun `given resource cancel when get value should return cancellation Error`() = runTest {
        val flow = MutableResourceStateFlow<TestData>()
        backgroundScope.launch {
            flow.load(Mutex()) {
                delay(200)
                currentCoroutineContext().cancel()
                error("This should not be thrown")
            }
        }

        testScheduler.advanceTimeBy(300)
        val loaded = assertIs<Resource.Loaded<TestData>>(flow.value)
        val error = assertIs<Result.Error>(loaded.result)
        assertTrue(error.isCancelled)
    }

    @Test
    fun `given resource uninitialized when reload multiple times should only load once`() = runTest {
        val flow = MutableResourceStateFlow<TestData>()
        val mutex = Mutex()

        backgroundScope.launch {
            flow.load(mutex) {
                delay(200)
                testData
            }
        }

        flow.test {
            assertEquals(Resource.Unloaded, awaitItem())

            testScheduler.advanceTimeBy(100)
            assertEquals(Resource.Loading, awaitItem())

            flow.load(mutex) { error("This should not be thrown") }
            expectNoEvents()

            testScheduler.advanceTimeBy(200)
            assertEquals(Resource.Success(testData), awaitItem())
        }
    }
}
