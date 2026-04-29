package app.vercors.shared.system

import kotlin.coroutines.CoroutineContext

expect class SystemServiceImpl : SystemService {
    override val osType: OsType
    override val osVersion: String
    override val osArch: OsArch
    override val canCopyToClipboard: Boolean
    override val canOpenUrlInBrowser: Boolean

    override fun copyToClipboard(text: String): Boolean
    override suspend fun openUrlInBrowser(url: String, context: CoroutineContext): Boolean
}
