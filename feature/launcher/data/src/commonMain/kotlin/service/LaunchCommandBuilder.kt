package app.vercors.feature.launcher.data.service

import app.vercors.core.domain.AppConstants
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.launcher.domain.model.LaunchVariables
import app.vercors.feature.launcher.domain.service.resolve
import app.vercors.feature.launcher.domain.service.substituteVariables
import app.vercors.shared.data.AppPaths
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named

/** Assembles the process command line for an instance whose dependencies are already resolved. */
@Inject
class LaunchCommandBuilder(
    private val appPaths: AppPaths,
    @Named(AppConstants.APP_NAME_KEY) private val launcherName: String,
    @Named(AppConstants.APP_VERSION_KEY) private val launcherVersion: String
) {
    fun build(
        dependencyList: InstanceDependencyList,
        account: Account,
        instance: Instance,
        version: GameVersionDetails,
        javaExecutable: String,
        jvmMemoryOptions: JvmMemoryOptions,
        nativeLibraryOptions: NativeLibraryOptions
    ): List<String> {
        val nativesDir = appPaths.nativesDir(instance.id)
        val classpath = buildList {
            addAll(dependencyList.libraries.map { it.path.toString() })
            if (!version.omitClientFromClasspath) add(dependencyList.client.path.toString())
            add(".")
        }.joinToString(separator = platformPathSeparator)
        val variables = LaunchVariables(
            account = account,
            instance = instance,
            version = version,
            classpath = classpath,
            gameDir = appPaths.gameDir(instance.id).toString(),
            assetsDir = appPaths.assets.toString(),
            nativesDir = nativesDir.toString(),
            libraryDir = appPaths.libraries.toString(),
            classpathSeparator = platformPathSeparator,
            launcherName = launcherName,
            launcherVersion = launcherVersion
        ).toMap()
        val jvmArgs = version.jvmArgs?.resolve(variables)
        val gameArgs = version.gameArgs?.resolve(variables)
        val logConfigArg = version.clientLogging?.argument?.substituteVariables(
            variables = mapOf("path" to dependencyList.logging!!.path.toString())
        )
        return buildList {
            add(javaExecutable)
            logConfigArg?.let { add(it) }
            addAll(jvmMemoryOptions.toJvmArgs())
            addAll(nativeLibraryOptions.toJvmArgs())
            jvmArgs?.let { addAll(it) } ?: run {
                add("-Djava.library.path=$nativesDir")
                add("-cp")
                add(classpath)
            }
            add(version.mainClass)
            gameArgs?.let { addAll(it) }
        }
    }
}

private fun JvmMemoryOptions.toJvmArgs(): List<String> = buildList {
    minMemoryMb?.let { add("-Xms${it}M") }
    maxMemoryMb?.let { add("-Xmx${it}M") }
    addAll(extraArgs.orEmpty().trim().split(Regex("\\s+")).filter(String::isNotBlank))
}

private fun NativeLibraryOptions.toJvmArgs(): List<String> = buildList {
    if (useSystemGlfw == true) add("-Dorg.lwjgl.glfw.libname=${glfwPath?.takeIf(String::isNotBlank) ?: "glfw"}")
    if (useSystemOpenAl == true) add("-Dorg.lwjgl.openal.libname=${openAlPath?.takeIf(String::isNotBlank) ?: "openal"}")
}
