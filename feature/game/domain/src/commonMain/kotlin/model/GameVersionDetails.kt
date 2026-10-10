package app.vercors.feature.game.domain.model

import kotlin.jvm.JvmInline

data class GameVersionDetails(
    val id: String,
    val type: GameVersionType,
    val clientDownload: Download,
    val serverDownload: Download?,
    val libraries: List<Library>,
    val javaVersion: JavaVersion?,
    val gameArgs: List<Argument>?,
    val jvmArgs: List<Argument>?,
    // Pre-fill for the user-editable JVM arguments, not required launch flags
    val defaultUserJvmArgs: List<Argument>?,
    val clientLogging: Logging?,
    val mainClass: String,
    val assets: String,
    val assetIndex: AssetIndex,
    // FML locates the client jar itself: putting it on the classpath breaks normal launches
    val omitClientFromClasspath: Boolean = false
) {
    sealed interface Argument {
        @JvmInline
        value class Basic(val value: String) : Argument
        data class Conditional(val rules: List<Rule>, val value: List<String>) : Argument
    }

    data class Rule(
        val action: Action,
        val features: Map<String, Boolean>? = null,
        val os: Os? = null,
    ) {
        enum class Action {
            Allow,
            Disallow
        }

        data class Os(
            val name: String? = null,
            val arch: String? = null,
            val version: String? = null,
            val versionRange: VersionRange? = null
        ) {
            // min is inclusive, max exclusive: Mojang uses the same bound on both sides of a split
            data class VersionRange(
                val min: String? = null,
                val max: String? = null
            )
        }
    }

    data class Download(
        val sha1: String,
        val size: Long,
        val url: String
    )

    data class JavaVersion(
        val component: String,
        val majorVersion: Int
    )

    data class Library(
        val name: String,
        val artifact: Artifact? = null,
        val classifiers: Map<String, Artifact>? = null,
        val natives: Map<String, String>? = null,
        val extractExclude: List<String>? = null,
        val rules: List<Rule>? = null
    ) {
        data class Artifact(
            val path: String,
            // Some loader repos omit hash/size: null skips validation
            val sha1: String?,
            val size: Long?,
            val url: String
        )
    }

    data class Logging(
        val argument: String,
        val file: File,
        val type: Type
    ) {
        enum class Type {
            LOG4J2_XML
        }

        data class File(
            val id: String,
            val sha1: String,
            val size: Long,
            val url: String
        )
    }

    data class AssetIndex(
        val id: String,
        val sha1: String,
        val size: Long,
        val totalSize: Long,
        val url: String
    )

    // A loader library wins over a vanilla one with the same group:artifact
    fun mergeWith(profile: LoaderInstallProfile): GameVersionDetails {
        val loaderCoordinates = profile.libraries.map { it.coordinate }.toSet()
        return copy(
            libraries = libraries.filterNot { it.coordinate in loaderCoordinates } + profile.libraries,
            gameArgs = if (profile.replacesGameArgs) {
                profile.gameArgs
            } else {
                (gameArgs.orEmpty() + profile.gameArgs.orEmpty()).ifEmpty { null }
            },
            jvmArgs = (jvmArgs.orEmpty() + profile.jvmArgs.orEmpty()).ifEmpty { null },
            mainClass = profile.mainClass,
            omitClientFromClasspath = omitClientFromClasspath || profile.omitClientFromClasspath
        )
    }

    private val Library.coordinate: String
        get() = name.substringBeforeLast(':')
}
