package app.vercors.feature.launcher.data.service

class InstanceDependencyList(
    val client: DependencyFile,
    val libraries: List<DependencyFile>,
    val natives: List<DependencyFile.Native>,
    val assets: List<DependencyFile>,
    val logging: DependencyFile?
) {
    val allDependencies: List<DependencyFile> = buildList {
        add(client)
        addAll(libraries)
        addAll(natives)
        addAll(assets)
        logging?.let { add(it) }
    }
}
