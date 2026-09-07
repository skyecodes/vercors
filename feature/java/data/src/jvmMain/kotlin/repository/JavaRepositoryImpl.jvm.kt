package app.vercors.feature.java.data.repository

import app.vercors.core.system.OsType
import app.vercors.core.system.SystemService
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.Win32Exception
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinReg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val baseRegistries = arrayOf(WinReg.HKEY_CURRENT_USER, WinReg.HKEY_LOCAL_MACHINE)

internal actual suspend fun platformScanJavaInstallationsWindows(systemService: SystemService): List<CandidateJavaPath> {
    if (systemService.osType != OsType.Windows) return emptyList()
    return withContext(Dispatchers.IO) {
        buildList {
            // Oracle
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\Java Runtime Environment", "JavaHome"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\Java Runtime Environment", "JavaHome"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\Java Development Kit", "JavaHome"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\Java Development Kit", "JavaHome"))

            // Oracle for Java 9 and newer
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\JRE", "JavaHome"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\JRE", "JavaHome"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\JavaSoft\\JDK", "JavaHome"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\JavaSoft\\JDK", "JavaHome"))

            // AdoptOpenJDK
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\AdoptOpenJDK\\JRE", "Path", "\\hotspot\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\AdoptOpenJDK\\JRE", "Path", "\\hotspot\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\AdoptOpenJDK\\JDK", "Path", "\\hotspot\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\AdoptOpenJDK\\JDK", "Path", "\\hotspot\\MSI"))

            // Eclipse Foundation
            addAll(
                scanRegistryKey(
                    WinNT.KEY_WOW64_64KEY,
                    "SOFTWARE\\Eclipse Foundation\\JDK",
                    "Path",
                    "\\hotspot\\MSI"
                )
            )
            addAll(
                scanRegistryKey(
                    WinNT.KEY_WOW64_32KEY,
                    "SOFTWARE\\Eclipse Foundation\\JDK",
                    "Path",
                    "\\hotspot\\MSI"
                )
            )

            // Eclipse Adoptium
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\Eclipse Adoptium\\JRE", "Path", "\\hotspot\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\Eclipse Adoptium\\JRE", "Path", "\\hotspot\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\Eclipse Adoptium\\JDK", "Path", "\\hotspot\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\Eclipse Adoptium\\JDK", "Path", "\\hotspot\\MSI"))

            // IBM Semeru
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\Semeru\\JRE", "Path", "\\openj9\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\Semeru\\JRE", "Path", "\\openj9\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\Semeru\\JDK", "Path", "\\openj9\\MSI"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\Semeru\\JDK", "Path", "\\openj9\\MSI"))

            // Microsoft
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\Microsoft\\JDK", "Path", "\\hotspot\\MSI"))

            // Azul Zulu
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\Azul Systems\\Zulu", "InstallationPath"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\Azul Systems\\Zulu", "InstallationPath"))

            // BellSoft Liberica
            addAll(scanRegistryKey(WinNT.KEY_WOW64_64KEY, "SOFTWARE\\BellSoft\\Liberica", "InstallationPath"))
            addAll(scanRegistryKey(WinNT.KEY_WOW64_32KEY, "SOFTWARE\\BellSoft\\Liberica", "InstallationPath"))

            listOf(
                "C:/Program Files/Java/jre8/bin/javaw.exe",
                "C:/Program Files/Java/jre7/bin/javaw.exe",
                "C:/Program Files/Java/jre6/bin/javaw.exe",
                "C:/Program Files (x86)/Java/jre8/bin/javaw.exe",
                "C:/Program Files (x86)/Java/jre7/bin/javaw.exe",
                "C:/Program Files (x86)/Java/jre6/bin/javaw.exe",
            ).filter { File(it).isFile }.forEach { add(CandidateJavaPath(it)) }

            addAll(scanPathEnvironment("java.exe", "javaw.exe"))
        }.distinct()
    }
}

private fun scanRegistryKey(
    keyType: Int,
    keyName: String,
    keyJavaDir: String,
    subkeySuffix: String = ""
): List<CandidateJavaPath> =
    baseRegistries.mapNotNull { baseRegistry ->
        try {
            val jreKey = Advapi32Util.registryGetKey(
                baseRegistry,
                keyName,
                WinNT.KEY_READ or keyType or WinNT.KEY_ENUMERATE_SUB_KEYS
            ).value
            try {
                val numSubKeys = Advapi32Util.registryQueryInfoKey(jreKey, 0).lpcSubKeys.value
                if (numSubKeys > 0) {
                    for (i in 0 until numSubKeys) {
                        val enumKey = Advapi32Util.registryRegEnumKey(jreKey, i)
                        val subKeyName = enumKey.lpName.joinToString()
                        val newKeyName = "$keyName\\$subKeyName$subkeySuffix"
                        val newKey =
                            Advapi32Util.registryGetKey(baseRegistry, newKeyName, WinNT.KEY_READ or keyType).value
                        try {
                            val value = Advapi32Util.registryGetStringValue(newKey, keyJavaDir)
                            return@mapNotNull CandidateJavaPath(File(value, "bin/javaw.exe").path.replace('\\', '/'))
                        } finally {
                            Advapi32Util.registryCloseKey(newKey)
                        }
                    }
                }
            } finally {
                Advapi32Util.registryCloseKey(jreKey)
            }
        } catch (_: Win32Exception) {
            // Silently ignore
        }
        null
    }

internal actual suspend fun platformScanJavaInstallationsLinux(systemService: SystemService): List<CandidateJavaPath> {
    if (systemService.osType != OsType.Linux) return emptyList()
    return withContext(Dispatchers.IO) {
        buildList {
            for (base in listOf("/usr/lib/jvm", "/usr/lib32/jvm", "/usr/lib64/jvm")) {
                File(base).listFiles()?.filter { it.isDirectory }?.forEach {
                    addIfExecutable(File(it, "bin/java"))
                }
            }
            File("/opt").listFiles()?.filter { it.isDirectory && (it.name.contains("jdk") || it.name.contains("jre")) }
                ?.forEach { addIfExecutable(File(it, "bin/java")) }

            System.getenv("JAVA_HOME")?.let { addIfExecutable(File(it, "bin/java")) }
            addAll(scanPathEnvironment("java"))
            addAll(scanUpdateAlternatives())
        }.distinct()
    }
}

private fun MutableList<CandidateJavaPath>.addIfExecutable(file: File) {
    if (file.isFile) add(CandidateJavaPath(file.path))
}

private fun scanUpdateAlternatives(): List<CandidateJavaPath> = try {
    val process = ProcessBuilder("update-alternatives", "--list", "java").redirectErrorStream(false).start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    output.lineSequence().map { it.trim() }.filter { it.isNotEmpty() && File(it).isFile }.map { CandidateJavaPath(it) }
        .toList()
} catch (_: Exception) {
    emptyList()
}

internal actual suspend fun platformScanJavaInstallationsMacOs(systemService: SystemService): List<CandidateJavaPath> {
    if (systemService.osType != OsType.MacOs) return emptyList()
    return withContext(Dispatchers.IO) {
        buildList {
            for (base in listOf("/Library/Java/JavaVirtualMachines", "/System/Library/Java/JavaVirtualMachines")) {
                File(base).listFiles()?.filter { it.isDirectory }?.forEach {
                    addIfExecutable(File(it, "Contents/Home/bin/java"))
                }
            }
            System.getenv("JAVA_HOME")?.let { addIfExecutable(File(it, "bin/java")) }
            addAll(scanPathEnvironment("java"))
            addAll(scanJavaHomeVerbose())
        }.distinct()
    }
}

private fun scanJavaHomeVerbose(): List<CandidateJavaPath> = try {
    val process = ProcessBuilder("/usr/libexec/java_home", "-V").redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    output.lineSequence()
        .mapNotNull { line -> Regex("(/\\S+/Contents/Home)").find(line)?.groupValues?.get(1) }
        .filter { File(it, "bin/java").isFile }
        .map { CandidateJavaPath(File(it, "bin/java").path) }
        .toList()
} catch (_: Exception) {
    emptyList()
}

/** Scans every directory on `PATH` for any of [executableNames]. */
private fun scanPathEnvironment(vararg executableNames: String): List<CandidateJavaPath> {
    val path = System.getenv("PATH") ?: return emptyList()
    val separator = if (File.pathSeparatorChar == ';') ";" else ":"
    return path.split(separator).flatMap { dir ->
        executableNames.map { File(dir, it) }.filter { it.isFile }.map { CandidateJavaPath(it.path) }
    }
}
