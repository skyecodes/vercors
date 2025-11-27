package app.vercors.meta.lib.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class GameVersionDetails(
    val id: String,
    val type: GameVersionType,
    val releaseTime: Instant,
    val minimumLauncherVersion: Int,
    val clientDownload: Download,
    val serverDownload: Download?,
    val assetIndex: AssetIndex,
    val libraries: List<Library>,
    val javaVersion: JavaVersion?,
    val gameArgs: List<Argument>?,
    val jvmArgs: List<Argument>?,
    val clientLogging: Logging?,
    val mainClass: String,
) {
    @Serializable
    sealed interface Argument

    @JvmInline
    @Serializable
    value class BasicArgument(val value: String) : Argument

    @Serializable
    data class ConditionalArgument(val rules: List<Rule>, val value: List<String>) : Argument

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

            @SerialName("deny")
            Deny
        }

        @Serializable
        data class Os(
            val name: String? = null,
            val arch: String? = null,
            val version: String? = null
        )
    }

    @Serializable
    data class AssetIndex(
        val id: String,
        val sha1: String,
        val size: Int,
        val totalSize: Int,
        val url: String
    )

    @Serializable
    data class Download(
        val sha1: String,
        val size: Int,
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
        val rules: List<Rule>? = null
    ) {
        @Serializable
        data class Artifact(
            val path: String,
            val sha1: String,
            val size: Int,
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
            val size: Int,
            val url: String
        )
    }
}