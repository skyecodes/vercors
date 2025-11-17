package app.vercors.shared.data

import kotlinx.io.files.Path

expect fun Path.createLink(target: Path, isSymbolic: Boolean = false): Boolean

