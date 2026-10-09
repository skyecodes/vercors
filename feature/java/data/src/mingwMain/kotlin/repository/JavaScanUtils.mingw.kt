@file:OptIn(ExperimentalForeignApi::class)

package app.vercors.feature.java.data.repository

import app.vercors.core.system.SystemService
import kotlinx.cinterop.*
import platform.windows.*

private data class RegistryRoot(val hive: HKEY?, val wow64Flag: UInt)

private val registryRoots: List<RegistryRoot>
    get() = listOf(
        RegistryRoot(HKEY_CURRENT_USER, KEY_WOW64_64KEY.toUInt()),
        RegistryRoot(HKEY_CURRENT_USER, KEY_WOW64_32KEY.toUInt()),
        RegistryRoot(HKEY_LOCAL_MACHINE, KEY_WOW64_64KEY.toUInt()),
        RegistryRoot(HKEY_LOCAL_MACHINE, KEY_WOW64_32KEY.toUInt()),
    )

internal actual suspend fun platformScanJavaInstallationsLinux(systemService: SystemService): List<CandidateJavaPath> =
    emptyList()

internal actual suspend fun platformScanJavaInstallationsMacOs(systemService: SystemService): List<CandidateJavaPath> =
    emptyList()

internal actual suspend fun platformScanJavaInstallationsWindows(systemService: SystemService): List<CandidateJavaPath> =
    buildList {
        addAll(scanRegistryKey("SOFTWARE\\JavaSoft\\Java Runtime Environment", "JavaHome"))
        addAll(scanRegistryKey("SOFTWARE\\JavaSoft\\Java Development Kit", "JavaHome"))

        addAll(scanRegistryKey("SOFTWARE\\JavaSoft\\JRE", "JavaHome"))
        addAll(scanRegistryKey("SOFTWARE\\JavaSoft\\JDK", "JavaHome"))

        addAll(scanRegistryKey("SOFTWARE\\AdoptOpenJDK\\JRE", "Path", "\\hotspot\\MSI"))
        addAll(scanRegistryKey("SOFTWARE\\AdoptOpenJDK\\JDK", "Path", "\\hotspot\\MSI"))

        addAll(scanRegistryKey("SOFTWARE\\Eclipse Foundation\\JDK", "Path", "\\hotspot\\MSI"))

        addAll(scanRegistryKey("SOFTWARE\\Eclipse Adoptium\\JRE", "Path", "\\hotspot\\MSI"))
        addAll(scanRegistryKey("SOFTWARE\\Eclipse Adoptium\\JDK", "Path", "\\hotspot\\MSI"))

        addAll(scanRegistryKey("SOFTWARE\\Semeru\\JRE", "Path", "\\openj9\\MSI"))
        addAll(scanRegistryKey("SOFTWARE\\Semeru\\JDK", "Path", "\\openj9\\MSI"))

        addAll(scanRegistryKey("SOFTWARE\\Microsoft\\JDK", "Path", "\\hotspot\\MSI"))

        addAll(scanRegistryKey("SOFTWARE\\Azul Systems\\Zulu", "InstallationPath"))

        addAll(scanRegistryKey("SOFTWARE\\BellSoft\\Liberica", "InstallationPath"))

        listOf(
            "C:/Program Files/Java/jre8/bin/javaw.exe",
            "C:/Program Files/Java/jre7/bin/javaw.exe",
            "C:/Program Files/Java/jre6/bin/javaw.exe",
            "C:/Program Files (x86)/Java/jre8/bin/javaw.exe",
            "C:/Program Files (x86)/Java/jre7/bin/javaw.exe",
            "C:/Program Files (x86)/Java/jre6/bin/javaw.exe",
        ).forEach { add(CandidateJavaPath(it)) }
    }.distinct()

private fun scanRegistryKey(keyName: String, keyJavaDir: String, subkeySuffix: String = ""): List<CandidateJavaPath> =
    registryRoots.mapNotNull { root ->
        val key = openKey(root.hive, keyName, root.wow64Flag) ?: return@mapNotNull null
        try {
            val subKeyName = firstSubKeyName(key) ?: return@mapNotNull null
            val newKeyName = "$keyName\\$subKeyName$subkeySuffix"
            val newKey = openKey(root.hive, newKeyName, root.wow64Flag) ?: return@mapNotNull null
            try {
                val javaHome = stringValue(newKey, keyJavaDir) ?: return@mapNotNull null
                CandidateJavaPath("${javaHome.replace('\\', '/').trimEnd('/')}/bin/javaw.exe")
            } finally {
                RegCloseKey(newKey)
            }
        } finally {
            RegCloseKey(key)
        }
    }

private fun openKey(hive: HKEY?, subKey: String, samFlags: UInt): HKEY? = memScoped {
    val result = alloc<HKEYVar>()
    val status = RegOpenKeyExW(
        hive,
        subKey,
        0u,
        KEY_READ.toUInt() or samFlags or KEY_ENUMERATE_SUB_KEYS.toUInt(),
        result.ptr
    )
    if (status != ERROR_SUCCESS) null else result.value
}

private fun firstSubKeyName(key: HKEY?): String? = memScoped {
    val buffer = allocArray<UShortVar>(256)
    val length = alloc<UIntVar>()
    length.value = 256u
    val status = RegEnumKeyExW(key, 0u, buffer, length.ptr, null, null, null, null)
    if (status != ERROR_SUCCESS) null else buffer.reinterpret<UShortVar>().toKStringFromUtf16()
}

private fun stringValue(key: HKEY?, valueName: String): String? = memScoped {
    val buffer = allocArray<UShortVar>(1024)
    val length = alloc<UIntVar>()
    length.value = 2048u
    val status = RegQueryValueExW(key, valueName, null, null, buffer.reinterpret(), length.ptr)
    if (status != ERROR_SUCCESS) null else buffer.reinterpret<UShortVar>().toKStringFromUtf16()
}
