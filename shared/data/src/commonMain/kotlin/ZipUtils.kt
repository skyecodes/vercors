package app.vercors.shared.data

import kotlinx.io.files.Path

// Zip entry names always use '/', but Path.toString() uses the platform separator ('\' on Windows)
fun Path.toZipEntryName(): String = parent?.let { "${it.toZipEntryName()}/$name" } ?: name
