package app.vercors.launcher.common.data.util

import kotlinx.coroutines.CoroutineDispatcher

actual suspend fun openUrlInBrowser(url: String, ioDispatcher: CoroutineDispatcher): OpenBrowserResult =
    OpenBrowserResult.Unsupported
