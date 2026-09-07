package app.vercors.feature.java.data.repository

import app.vercors.core.filesystem.exists
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import platform.posix.getenv

/** Shared by the Linux and macOS candidate-path scanners — both resolve `JAVA_HOME`/`PATH` the same POSIX way. */
@OptIn(ExperimentalForeignApi::class)
internal fun getEnvVar(name: String): String? = getenv(name)?.toKString()

internal suspend fun MutableList<CandidateJavaPath>.addIfExecutable(path: Path) {
    if (path.exists()) add(CandidateJavaPath(path.toString()))
}

@OptIn(ExperimentalForeignApi::class)
internal fun pathExistsBlocking(path: Path): Boolean = platform.posix.access(path.toString(), platform.posix.F_OK) == 0

internal fun scanPathEnvironment(vararg executableNames: String): List<CandidateJavaPath> {
    val path = getEnvVar("PATH") ?: return emptyList()
    return path.split(":").flatMap { dir ->
        executableNames.map { Path(dir, it) }.filter { pathExistsBlocking(it) }.map { CandidateJavaPath(it.toString()) }
    }
}
