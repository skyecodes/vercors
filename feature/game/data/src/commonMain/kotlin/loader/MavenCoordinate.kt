package app.vercors.feature.game.data.loader

/**
 * Resolves a maven coordinate (`group:artifact:version[:classifier][@ext]`) to the relative path it occupies
 * in a maven-layout library directory, e.g. `net.minecraftforge:forge:1.20.1-47.2.0:client` becomes
 * `net/minecraftforge/forge/1.20.1-47.2.0/forge-1.20.1-47.2.0-client.jar`.
 */
internal fun mavenCoordinateToPath(coordinate: String): String {
    val ext = coordinate.substringAfterLast('@', "jar")
    val base = coordinate.substringBeforeLast('@')
    val parts = base.split(':')
    val group = parts[0]
    val artifact = parts[1]
    val version = parts[2]
    val classifier = parts.getOrNull(3)
    val fileName = buildString {
        append(artifact).append('-').append(version)
        if (classifier != null) append('-').append(classifier)
        append('.').append(ext)
    }
    return "${group.replace('.', '/')}/$artifact/$version/$fileName"
}
