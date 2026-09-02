package app.vercors.core.filesystem

import kotlinx.cinterop.*
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.windows.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant

@OptIn(ExperimentalForeignApi::class)
actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    // CreateSymbolicLinkW isn't implemented — linkOrCopyTo's final fallback (a real copy) is an acceptable
    // substitute for the rare paths that specifically request a symlink.
    if (isSymbolic) return false
    // withContext's lambda has CoroutineScope as its implicit receiver, which shadows bare Path member calls
    // like toString() — capture both path strings before entering it (same footgun as realPath below).
    val existingFileName = toString()
    val newFileName = target.toString()
    return withContext(context) {
        // Win32 argument order is reversed from POSIX link(): new link name first, existing file second.
        CreateHardLinkW(newFileName, existingFileName, null) != 0
    }
}

actual suspend fun Path.lastModifiedOrNull(context: CoroutineContext): Instant? {
    // TODO find out a way to read a file's last-modified time on mingw
    return null
}

/** Windows has no executable permission bit — an `.exe` extension is sufficient. */
actual suspend fun Path.setExecutable(context: CoroutineContext): Boolean = true

/** TODO: Windows has no POSIX permission bits; real restriction needs ACL manipulation
 *  (SetNamedSecurityInfoW + current-user SID lookup), not implemented yet. */
actual suspend fun Path.restrictToOwner(context: CoroutineContext): Boolean = false

/** Resolves NTFS symlinks/junctions via a real handle, unlike `_fullpath`, which only normalizes the path
 *  textually without touching reparse points. */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun Path.realPath(context: CoroutineContext): Path? {
    // withContext's lambda has CoroutineScope as its implicit receiver, which shadows this Path receiver for bare
    // member calls like toString() — capture the path string first so we never call the wrong toString().
    val path = toString()
    return withContext(context) {
        val handle = CreateFileW(
            path,
            0u,
            (FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE).toUInt(),
            null,
            OPEN_EXISTING.toUInt(),
            FILE_FLAG_BACKUP_SEMANTICS.toUInt(),
            null
        )
        if (handle == null || handle == INVALID_HANDLE_VALUE) return@withContext null
        try {
            memScoped {
                val bufferSize = 4096
                val buffer = allocArray<UShortVar>(bufferSize)
                val length = GetFinalPathNameByHandleW(handle, buffer, bufferSize.toUInt(), 0u)
                if (length == 0u || length > bufferSize.toUInt()) null
                else Path(buffer.reinterpret<UShortVar>().toKStringFromUtf16().removePrefix("""\\?\"""))
            }
        } finally {
            CloseHandle(handle)
        }
    }
}
