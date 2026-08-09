package app.vercors.feature.launcher.data.service

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.launcher.domain.model.LaunchVariables
import app.vercors.feature.launcher.domain.service.resolve
import app.vercors.feature.launcher.domain.service.substituteVariables
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.Inject

/** Assembles the process command line for an instance whose dependencies are already resolved. */
@Inject
class LaunchCommandBuilder(private val appPaths: AppPaths) {
    fun build(
        dependencyList: InstanceDependencyList,
        account: Account,
        instance: Instance,
        version: GameVersionDetails
    ): List<String> {
        val nativesDir = appPaths.nativesDir(instance.id)
        val classpath = buildList {
            addAll(dependencyList.libraries.map { it.path.toString() })
            add(dependencyList.client.path.toString())
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
        ).toMap()
        val jvmArgs = version.jvmArgs?.resolve(variables)
        val gameArgs = version.gameArgs?.resolve(variables)
        val logConfigArg = version.clientLogging?.argument?.substituteVariables(
            variables = mapOf("path" to dependencyList.logging!!.path.toString())
        )
        return buildList {
            add("java")
            logConfigArg?.let { add(it) }
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
