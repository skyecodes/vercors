package app.vercors.website.data

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.Promise

suspend fun detectPlatform(): Platform {
    val userAgentData = window.navigator.asDynamic().userAgentData
    val userAgent = window.navigator.userAgent
    val platformName = (userAgentData?.platform as? String)?.takeIf { it.isNotEmpty() } ?: userAgent
    val os = when {
        platformName.contains("Win", ignoreCase = true) -> Os.Windows
        platformName.contains("Mac", ignoreCase = true) -> Os.MacOs
        platformName.contains("Android", ignoreCase = true) -> null
        platformName.contains("Linux", ignoreCase = true) -> Os.Linux
        else -> null
    }
    val arch = when {
        os == Os.MacOs -> Arch.Arm64
        userAgentData != null -> detectArch(userAgentData)
        userAgent.contains("aarch64") || userAgent.contains("arm64", ignoreCase = true) -> Arch.Arm64
        else -> Arch.X64
    }
    return Platform(os, arch)
}

// Only Chromium exposes the CPU architecture; everything else defaults to x64
private suspend fun detectArch(userAgentData: dynamic): Arch = runCatching {
    val values = (userAgentData.getHighEntropyValues(arrayOf("architecture")) as Promise<dynamic>).await()
    if (values.architecture == "arm") Arch.Arm64 else Arch.X64
}.getOrDefault(Arch.X64)
