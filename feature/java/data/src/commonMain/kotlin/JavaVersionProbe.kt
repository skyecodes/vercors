package app.vercors.feature.java.data

import app.vercors.feature.java.domain.model.JavaProbeResult

internal expect suspend fun probeJavaExecutable(path: String): JavaProbeResult?

private val PROPERTY_REGEX = Regex("""^\s*(java\.version|os\.arch|java\.vendor)\s*=\s*(.+?)\s*$""")

internal fun parseJavaProbeOutput(output: String): JavaProbeResult? {
    val properties = output.lineSequence()
        .mapNotNull { PROPERTY_REGEX.find(it) }
        .associate { it.groupValues[1] to it.groupValues[2] }
    val versionString = properties["java.version"] ?: return null
    val majorVersion = versionString.toJavaMajorVersion() ?: return null
    return JavaProbeResult(
        versionString = versionString,
        majorVersion = majorVersion,
        vendor = properties["java.vendor"] ?: "Unknown",
        arch = properties["os.arch"] ?: "unknown",
    )
}

internal fun String.toJavaMajorVersion(): Int? {
    val firstComponent = substringBefore('.').substringBefore('_').toIntOrNull() ?: return null
    return if (firstComponent == 1) substringAfter('.').substringBefore('.').substringBefore('_').toIntOrNull()
    else firstComponent
}
