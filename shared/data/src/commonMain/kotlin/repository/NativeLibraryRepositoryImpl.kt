package app.vercors.shared.data.repository

import app.vercors.core.filesystem.exists
import app.vercors.shared.domain.model.NativeLibraryType
import app.vercors.shared.domain.repository.NativeLibraryRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.io.files.Path

private val LIBRARY_SEARCH_DIRS = listOf(
    "/usr/lib",
    "/usr/lib64",
    "/usr/lib/x86_64-linux-gnu",
    "/usr/lib/aarch64-linux-gnu",
    "/usr/local/lib",
    "/usr/local/lib64",
    "/lib",
    "/lib64",
)

private val CANDIDATE_FILE_NAMES = mapOf(
    NativeLibraryType.Glfw to listOf("libglfw.so", "libglfw.so.3"),
    NativeLibraryType.OpenAl to listOf("libopenal.so", "libopenal.so.1"),
)

@Inject
@ContributesBinding(AppScope::class)
class NativeLibraryRepositoryImpl : NativeLibraryRepository {
    override suspend fun detectSystemLibraryPath(type: NativeLibraryType): String? {
        val candidateFileNames = CANDIDATE_FILE_NAMES.getValue(type)
        for (dir in LIBRARY_SEARCH_DIRS) {
            for (fileName in candidateFileNames) {
                val path = Path(dir, fileName)
                if (path.exists()) return path.toString()
            }
        }
        return null
    }
}
