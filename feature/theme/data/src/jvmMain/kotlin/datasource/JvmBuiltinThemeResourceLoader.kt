package app.vercors.feature.theme.data.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class JvmBuiltinThemeResourceLoader : BuiltinThemeResourceLoader {
    override suspend fun loadResourceFromPath(path: String): String? {
        val normalized = path.removePrefix("/")
        val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream(normalized)
            ?: JvmBuiltinThemeResourceLoader::class.java.classLoader?.getResourceAsStream(normalized)
            ?: return null

        return stream.use { it.bufferedReader().readText() }
    }
}
