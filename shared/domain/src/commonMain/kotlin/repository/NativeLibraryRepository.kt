package app.vercors.shared.domain.repository

import app.vercors.shared.domain.model.NativeLibraryType

interface NativeLibraryRepository {
    suspend fun detectSystemLibraryPath(type: NativeLibraryType): String?
}
