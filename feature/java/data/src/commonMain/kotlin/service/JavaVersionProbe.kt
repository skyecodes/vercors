package app.vercors.feature.java.data.service

import app.vercors.feature.java.domain.model.JavaProbeResult

/** Spawns `<path> -XshowSettings:properties -version` and captures its combined stdout+stderr output to determine
 *  a candidate binary's real version/arch/vendor, mirroring PrismLauncher's Java-checking approach. Returns null if
 *  the binary can't be run or doesn't look like a Java executable. */
internal expect suspend fun probeJavaExecutable(path: String): JavaProbeResult?

private val propertyRegex = Regex("""^\s*(java\.version|os\.arch|java\.vendor)\s*=\s*(.+?)\s*$""")

internal fun parseJavaProbeOutput(output: String): JavaProbeResult? {
    val properties = output.lineSequence()
        .mapNotNull { propertyRegex.find(it) }
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

/** Parses a `java.version` string into its major version, handling both the legacy `1.8.0_392` (major = second
 *  component) and modern `17.0.9` (major = first component) formats. */
internal fun String.toJavaMajorVersion(): Int? {
    val firstComponent = substringBefore('.').substringBefore('_').toIntOrNull() ?: return null
    return if (firstComponent == 1) substringAfter('.').substringBefore('.').substringBefore('_').toIntOrNull()
    else firstComponent
}
