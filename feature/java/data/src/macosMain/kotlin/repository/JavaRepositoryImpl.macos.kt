package app.vercors.feature.java.data.repository

import app.vercors.core.filesystem.isDirectory
import app.vercors.core.filesystem.list
import app.vercors.core.system.SystemService
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

internal actual suspend fun platformScanJavaInstallationsWindows(systemService: SystemService): List<CandidateJavaPath> =
    emptyList()

internal actual suspend fun platformScanJavaInstallationsLinux(systemService: SystemService): List<CandidateJavaPath> =
    emptyList()

internal actual suspend fun platformScanJavaInstallationsMacOs(systemService: SystemService): List<CandidateJavaPath> =
    buildList {
        for (base in listOf("/Library/Java/JavaVirtualMachines", "/System/Library/Java/JavaVirtualMachines")) {
            val baseDir = Path(base)
            if (baseDir.isDirectory()) {
                baseDir.list().forEach { addIfExecutable(Path(it, "Contents", "Home", "bin", "java")) }
            }
        }
        getEnvVar("JAVA_HOME")?.let { addIfExecutable(Path(it, "bin", "java")) }
        addAll(scanPathEnvironment("java"))
        addAll(scanJavaHomeVerbose())
    }.distinct()

@OptIn(ExperimentalForeignApi::class)
internal fun scanJavaHomeVerbose(): List<CandidateJavaPath> {
    val process = popen("/usr/libexec/java_home -V 2>&1", "r") ?: return emptyList()
    val output = StringBuilder()
    val buffer = ByteArray(4096)
    while (true) {
        val line = fgets(buffer.refTo(0), buffer.size, process) ?: break
        output.append(line.toKString())
    }
    pclose(process)
    return output.lineSequence()
        .mapNotNull { line -> Regex("(/\\S+/Contents/Home)").find(line)?.groupValues?.get(1) }
        .map { Path(it, "bin", "java") }
        .filter { pathExistsBlocking(it) }
        .map { CandidateJavaPath(it.toString()) }
        .toList()
}
