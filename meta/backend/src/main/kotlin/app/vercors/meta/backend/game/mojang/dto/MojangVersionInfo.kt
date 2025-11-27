package app.vercors.meta.backend.game.mojang.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MojangVersionInfo(
    val arguments: Arguments? = null,
    val assetIndex: AssetIndex,
    val assets: String,
    val complianceLevel: Int? = null,
    val downloads: Downloads,
    val id: String,
    val javaVersion: JavaVersion? = null,
    val libraries: List<Library>,
    val logging: Logging? = null,
    val mainClass: String,
    val minecraftArguments: String? = null,
    val minimumLauncherVersion: Int,
    val releaseTime: MojangInstant,
    val time: MojangInstant,
    val type: MojangReleaseType
) {
    @Serializable
    data class Arguments(
        val game: List<MojangArgument>,
        val jvm: List<MojangArgument>
    )

    @Serializable
    data class AssetIndex(
        val id: String,
        override val sha1: String,
        override val size: Int,
        val totalSize: Int,
        override val url: String
    ) : MojangFile

    @Serializable
    data class Downloads(
        val client: Download,
        @SerialName("client_mappings")
        val clientMappings: Download? = null,
        val server: Download? = null,
        @SerialName("server_mappings")
        val serverMappings: Download? = null,
        @SerialName("windows_server")
        val windowsServer: Download? = null
    ) {
        @Serializable
        data class Download(
            override val sha1: String,
            override val size: Int,
            override val url: String
        ) : MojangFile
    }

    @Serializable
    data class JavaVersion(
        val component: String,
        val majorVersion: Int
    )

    @Serializable
    data class Library(
        val downloads: Downloads,
        val extract: Extract? = null,
        val name: String,
        val natives: Map<String, String>? = null,
        val rules: List<Rule>? = null
    ) {
        @Serializable
        data class Downloads(
            val artifact: Artifact? = null,
            val classifiers: Map<String, Artifact>? = null
        ) {
            @Serializable
            data class Artifact(
                val path: String,
                override val sha1: String,
                override val size: Int,
                override val url: String
            ) : MojangFile
        }

        @Serializable
        data class Extract(
            val exclude: List<String>
        )
    }

    @Serializable
    data class Logging(
        val client: Client
    ) {
        @Serializable
        data class Client(
            val argument: String,
            val file: File,
            val type: Type
        )

        enum class Type {
            @SerialName("log4j2-xml")
            Log4j2Xml
        }

        @Serializable
        data class File(
            val id: String,
            override val sha1: String,
            override val size: Int,
            override val url: String
        ) : MojangFile
    }

    @Serializable
    data class Rule(
        val action: Action,
        val os: Os? = null,
        val features: Map<String, Boolean>? = null
    ) {
        @Serializable
        data class Os(
            val name: String? = null,
            val version: String? = null,
            val arch: String? = null
        )

        enum class Action {
            @SerialName("allow")
            Allow,

            @SerialName("disallow")
            Disallow
        }
    }
}
