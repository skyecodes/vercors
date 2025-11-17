package app.vercors.shared.data

import kotlinx.io.files.Path

actual fun Path.createLink(target: Path, isSymbolic: Boolean): Boolean {
    return false
}