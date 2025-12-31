package app.vercors.meta.backend.game

import app.vercors.meta.lib.game.MojangReleaseType
import app.vercors.meta.lib.game.MojangVersionInfo
import app.vercors.meta.lib.game.MojangVersionManifest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlin.test.Test

class GameServiceTest {
    @Test
    fun extractAllVersions() = runTest {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
        }
        val manifest = httpClient.get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
            .body<MojangVersionManifest>()
        val dispatcher = StandardTestDispatcher(testScheduler).limitedParallelism(32)
        val allVersionData = manifest.versions.map {
            withContext(dispatcher) {
                async {
                    httpClient.get(it.url).body<JsonObject>()
                }
            }
        }.awaitAll()
        val json = Json { prettyPrint = true }
        SystemFileSystem.sink(Path("all_versions.json")).buffered().use { json.encodeToSink(allVersionData, it) }
    }

    @Test
    fun extractMinecraftArgumentVariables() = runTest {
        val allVersionData: List<MojangVersionInfo> =
            SystemFileSystem.source(Path("all_versions.json")).buffered().use { Json.decodeFromSource(it) }
        val strings: List<String> = allVersionData
            .flatMap { version ->
                listOf(version.minecraftArguments) +
                        extractArguments(version.arguments?.game) +
                        extractArguments(version.arguments?.jvm) +
                        version.logging?.client?.argument
            }
            .distinct().filterNotNull().filter { '$' in it }
        val regex = Regex("\\$\\{(.*?)}")
        strings.flatMap { regex.findAll(it).flatMap { it.groupValues.drop(1) } }.distinct().sorted()
            .forEach { println(it) }
    }

    @Test
    fun extractJavaVersions() = runTest {
        val allVersionData: List<MojangVersionInfo> =
            SystemFileSystem.source(Path("all_versions.json")).buffered().use { Json.decodeFromSource(it) }
        allVersionData.filter { it.javaVersion != null }.groupBy { it.javaVersion }
            .mapValues { (_, value) ->
                value.filter { it.type == MojangReleaseType.Release }.minByOrNull { it.releaseTime }?.id
            }
            .forEach { (key, value) -> println("Java $key: $value") }
    }

    private fun extractArguments(args: List<MojangVersionInfo.Argument>?): Iterable<String?> = args?.flatMap { arg ->
        when (arg) {
            is MojangVersionInfo.Argument.Basic -> listOf(arg.value)
            is MojangVersionInfo.Argument.Conditional -> arg.value
        } as List<String?>
    } ?: emptyList()

    @Test
    fun extractRules() = runTest {
        val allVersionData: List<MojangVersionInfo> =
            SystemFileSystem.source(Path("all_versions.json")).buffered().use { Json.decodeFromSource(it) }
        allVersionData.flatMap {
            it.libraries.mapNotNull { it.rules } +
                    it.arguments?.game?.filterIsInstance<MojangVersionInfo.Argument.Conditional>()?.map { it.rules } +
                    it.arguments?.jvm?.filterIsInstance<MojangVersionInfo.Argument.Conditional>()?.map { it.rules }
        }.filterNotNull().flatten().distinct().forEach { println(it) }
    }

    @Test
    fun extractWindowsRule() = runTest {
        val allVersionData: List<MojangVersionInfo> =
            SystemFileSystem.source(Path("all_versions.json")).buffered().use { Json.decodeFromSource(it) }
        allVersionData.flatMap {
            it.libraries.filter { it.rules?.any { it.os != null && it.os!!.name == "windows" && it.os!!.version != null } == true } +
                    it.arguments?.game?.filterIsInstance<MojangVersionInfo.Argument.Conditional>()
                        ?.filter { it.rules.any { it.os != null && it.os!!.name == "windows" && it.os!!.version != null } } +
                    it.arguments?.jvm?.filterIsInstance<MojangVersionInfo.Argument.Conditional>()
                        ?.filter { it.rules.any { it.os != null && it.os!!.name == "windows" && it.os!!.version != null } }
        }.filterNotNull().distinct().forEach { println(it) }
    }

    @Test
    fun extractLibsWithoutArtifact() = runTest {
        val allVersionData: List<MojangVersionInfo> =
            SystemFileSystem.source(Path("all_versions.json")).buffered().use { Json.decodeFromSource(it) }
        allVersionData.flatMap {
            it.libraries.mapNotNull {
                it.downloads.classifiers
            }
        }.distinct().forEach { println(it) }
    }


    @Test
    fun extractNativeExcludes() = runTest {
        val allVersionData: List<MojangVersionInfo> =
            SystemFileSystem.source(Path("all_versions.json")).buffered().use { Json.decodeFromSource(it) }
        allVersionData.flatMap {
            it.libraries.flatMap {
                it.extract?.exclude ?: emptyList()
            }
        }.distinct().forEach { println(it) }
    }
}
