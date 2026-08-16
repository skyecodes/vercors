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
    val clientLogging: Logging?,
    val mainClass: String,
    val assets: String,
    val assetIndex: AssetIndex,
    // FML-based loaders (Forge/NeoForge) locate the (patched) client and their own jar themselves, by
    // scanning the "library_directory" launch variable for well-known maven coordinates — putting those on
    // the classpath instead makes FML think it's running inside a Gradle modding workspace, not a normal
    // launch. When set, the command builder omits the client jar from the classpath.
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
            val version: String? = null
        )
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
            // Not every loader repo publishes a hash/size for every library (e.g. Fabric's meta API omits
            // both for its own loader/intermediary jars) — null skips validation instead of always failing it.
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

    /**
     * Merges a mod loader's [profile] on top of this vanilla version: the loader's main class replaces the
     * vanilla one, its libraries are appended (a loader-supplied artifact wins over a vanilla one for the same
     * `group:artifact`), and its arguments are appended after the vanilla ones.
     */
    fun mergeWith(profile: LoaderInstallProfile): GameVersionDetails {
        val loaderCoordinates = profile.libraries.map { it.coordinate }.toSet()
        return copy(
            libraries = libraries.filterNot { it.coordinate in loaderCoordinates } + profile.libraries,
            gameArgs = (gameArgs.orEmpty() + profile.gameArgs.orEmpty()).ifEmpty { null },
            jvmArgs = (jvmArgs.orEmpty() + profile.jvmArgs.orEmpty()).ifEmpty { null },
            mainClass = profile.mainClass,
            omitClientFromClasspath = omitClientFromClasspath || profile.omitClientFromClasspath
        )
    }

    private val Library.coordinate: String
        get() = name.substringBeforeLast(':')
}
