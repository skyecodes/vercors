package app.vercors.feature.config.domain.model

enum class UpdateChannel {
    Stable,
    Beta,
    Alpha;

    companion object {
        fun fromVersion(version: String): UpdateChannel {
            val prerelease = version.substringAfter('-', missingDelimiterValue = "")
            return when {
                prerelease.startsWith("alpha") -> Alpha
                prerelease.startsWith("beta") -> Beta
                else -> Stable
            }
        }
    }
}
