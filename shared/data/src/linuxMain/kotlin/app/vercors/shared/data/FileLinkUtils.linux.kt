package app.vercors.shared.data

import kotlinx.io.files.Path
import platform.posix.link
import platform.posix.symlink

actual fun Path.createLink(target: Path, isSymbolic: Boolean): Boolean {
    if (isSymbolic) symlink(toString(), target.toString())
    else link(toString(), target.toString())
    return true
}