package app.vercors.launcher.common.data.system

import kotlinx.coroutines.CoroutineDispatcher

internal actual suspend fun openUrlInBrowser(url: String, ioDispatcher: CoroutineDispatcher): OpenBrowserResult =
    OpenBrowserResult.Unsupported
