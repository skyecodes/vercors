package app.vercors.meta.lib.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class GameVersionDetailsData(
    val id: String,
    val type: GameVersionTypeData,
    val releaseTime: Instant,
    val minimumLauncherVersion: Int,
    val clientDownload: Download,
    val serverDownload: Download?,
    val libraries: List<Library>,
    val javaVersion: JavaVersion?,
    val gameArgs: List<Argument>?,
    val jvmArgs: List<Argument>?,
    val clientLogging: Logging?,
    val mainClass: String,
    val assetsId: String,
    val assets: List<Asset>
) {
    @Serializable
    sealed interface Argument {
        @Serializable
        data class Basic(val value: String) : Argument

        @Serializable
        data class Conditional(val rules: List<Rule>, val value: List<String>) : Argument
    }

    @Serializable
    data class Rule(
        val action: Action,
        val features: Map<String, Boolean>? = null,
        val os: Os? = null,
    ) {
        @Serializable
        enum class Action {
            @SerialName("allow")
            Allow,

            @SerialName("disallow")
            Disallow
        }

        @Serializable
        data class Os(
            val name: String? = null,
            val arch: String? = null,
            val version: String? = null
        )
    }

    @Serializable
    data class Download(
        val sha1: String,
        val size: Long,
        val url: String
    )

    @Serializable
    data class JavaVersion(
        val component: String,
        val majorVersion: Int
    )

    @Serializable
    data class Library(
        val name: String,
        val artifact: Artifact? = null,
        val classifiers: Map<String, Artifact>? = null,
        val natives: Map<String, String>? = null,
        val extractExclude: List<String>? = null,
        val rules: List<Rule>? = null
    ) {
        @Serializable
        data class Artifact(
            val path: String,
            val sha1: String,
            val size: Long,
            val url: String
        )
    }

    @Serializable
    data class Logging(
        val argument: String,
        val file: File,
        val type: Type
    ) {
        @Serializable
        enum class Type {
            @SerialName("log4j2-xml")
            LOG4J2_XML
        }

        @Serializable
        data class File(
            val id: String,
            val sha1: String,
            val size: Long,
            val url: String
        )
    }

    @Serializable
    data class Asset(
        val hash: String,
        val size: Long
    )
}
