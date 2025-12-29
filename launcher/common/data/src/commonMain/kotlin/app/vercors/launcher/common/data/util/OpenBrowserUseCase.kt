package app.vercors.launcher.common.data.util

import app.vercors.launcher.common.data.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Factory

@Factory
class OpenBrowserUseCase(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(url: String): OpenBrowserResult = openUrlInBrowser(url, ioDispatcher)
}

sealed interface OpenBrowserResult {
    data object Unsupported : OpenBrowserResult
    data object Success : OpenBrowserResult
    data class Failure(val exception: Exception) : OpenBrowserResult
}

expect suspend fun openUrlInBrowser(url: String, ioDispatcher: CoroutineDispatcher): OpenBrowserResult
