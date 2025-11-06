package app.vercors.shared.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ResourceTest {
    data class TestData(
        val testValue: String
    )

    private val mutex = Mutex()

    @Test
    fun `given basic resource loaded when get value should return Loading while loading and Loaded when loaded`() =
        runTest {
            val resourceFlow = MutableResourceStateFlow<TestData, Throwable>()
            val testData = TestData("test")

            launch {
                resourceFlow.load(mutex) {
                    delay(2000)
                    testData
                }
            }

            testScheduler.runCurrent()
            testScheduler.advanceTimeBy(1.seconds)
            assertEquals(Resource.Loading, resourceFlow.value)

            testScheduler.advanceUntilIdle()
            assertTrue(resourceFlow.value is Resource.Success)
            assertEquals(testData.testValue, (resourceFlow.value as Resource.Success<TestData>).value.testValue)
        }

    @Test
    fun `given resource loading error when get value should return Error`() = runTest {
        val resourceFlow = MutableResourceStateFlow<TestData, Throwable>()
        val testException = Exception("Test error")

        launch {
            resourceFlow.load(mutex) {
                delay(2000)
                throw testException
            }
        }

        testScheduler.runCurrent()
        testScheduler.advanceTimeBy(1.seconds)
        assertEquals(Resource.Loading, resourceFlow.value)

        testScheduler.advanceUntilIdle()
        assertTrue(resourceFlow.value is Resource.Error)
        assertSame(testException.message, (resourceFlow.value as Resource.Error<Throwable>).error.message)
    }
}