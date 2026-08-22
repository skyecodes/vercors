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

internal actual suspend fun platformScanJavaInstallationsMacOs(systemService: SystemService): List<CandidateJavaPath> =
    emptyList()

internal actual suspend fun platformScanJavaInstallationsLinux(systemService: SystemService): List<CandidateJavaPath> =
    buildList {
        for (base in listOf("/usr/lib/jvm", "/usr/lib32/jvm", "/usr/lib64/jvm")) {
            val baseDir = Path(base)
            if (baseDir.isDirectory()) {
                baseDir.list().forEach { addIfExecutable(Path(it, "bin", "java")) }
            }
        }
        val opt = Path("/opt")
        if (opt.isDirectory()) {
            opt.list()
                .filter { it.name.contains("jdk") || it.name.contains("jre") }
                .forEach { addIfExecutable(Path(it, "bin", "java")) }
        }
        getEnvVar("JAVA_HOME")?.let { addIfExecutable(Path(it, "bin", "java")) }
        addAll(scanPathEnvironment("java"))
        addAll(scanUpdateAlternatives())
    }.distinct()

@OptIn(ExperimentalForeignApi::class)
internal fun getEnvVar(name: String): String? = getenv(name)?.toKString()

private suspend fun MutableList<CandidateJavaPath>.addIfExecutable(path: Path) {
    if (path.exists()) add(CandidateJavaPath(path.toString()))
}

internal fun scanPathEnvironment(vararg executableNames: String): List<CandidateJavaPath> {
    val path = getEnvVar("PATH") ?: return emptyList()
    return path.split(":").flatMap { dir ->
        executableNames.map { Path(dir, it) }.filter { pathExistsBlocking(it) }.map { CandidateJavaPath(it.toString()) }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun pathExistsBlocking(path: Path): Boolean = platform.posix.access(path.toString(), platform.posix.F_OK) == 0

@OptIn(ExperimentalForeignApi::class)
internal fun scanUpdateAlternatives(): List<CandidateJavaPath> {
    val process = popen("update-alternatives --list java 2>/dev/null", "r") ?: return emptyList()
    val output = StringBuilder()
    val buffer = ByteArray(4096)
    while (true) {
        val line = fgets(buffer.refTo(0), buffer.size, process) ?: break
        output.append(line.toKString())
    }
    pclose(process)
    return output.lineSequence().map { it.trim() }.filter { it.isNotEmpty() && pathExistsBlocking(Path(it)) }
        .map { CandidateJavaPath(it) }.toList()
}
