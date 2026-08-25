package app.vercors.shared.domain.repository

import app.vercors.shared.domain.model.NativeLibraryType

interface NativeLibraryRepository {
    /** Detects the absolute path of a system-installed [type] shared library, or `null` if none was found. */
    suspend fun detectSystemLibraryPath(type: NativeLibraryType): String?
}
