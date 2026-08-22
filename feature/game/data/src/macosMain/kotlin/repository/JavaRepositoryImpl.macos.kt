package app.vercors.feature.game.data.repository

import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.isDirectory
import app.vercors.core.filesystem.list
import app.vercors.core.system.SystemService
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.fgets
import platform.posix.getenv
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
private fun getEnvVar(name: String): String? = getenv(name)?.toKString()

private suspend fun MutableList<CandidateJavaPath>.addIfExecutable(path: Path) {
    if (path.exists()) add(CandidateJavaPath(path.toString()))
}

private fun pathExistsBlocking(path: Path): Boolean = platform.posix.access(path.toString(), platform.posix.F_OK) == 0

private fun scanPathEnvironment(vararg executableNames: String): List<CandidateJavaPath> {
    val path = getEnvVar("PATH") ?: return emptyList()
    return path.split(":").flatMap { dir ->
        executableNames.map { Path(dir, it) }.filter { pathExistsBlocking(it) }.map { CandidateJavaPath(it.toString()) }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun scanJavaHomeVerbose(): List<CandidateJavaPath> {
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
