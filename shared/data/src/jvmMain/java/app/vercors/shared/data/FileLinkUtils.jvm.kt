package app.vercors.shared.data

import kotlinx.io.files.Path
import kotlin.io.path.createLinkPointingTo
import kotlin.io.path.createSymbolicLinkPointingTo

actual fun Path.createLink(target: Path, isSymbolic: Boolean): Boolean {
    val src = kotlin.io.path.Path(toString())
    val dst = kotlin.io.path.Path(target.toString())
    if (isSymbolic) src.createSymbolicLinkPointingTo(dst) else src.createLinkPointingTo(dst)
    return true
}