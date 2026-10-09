package app.vercors.core.system

import dev.zacsweers.metro.*

@ContributesTo(AppScope::class)
@BindingContainer
object SystemProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideOsInfo(systemService: SystemService): OsInfo = systemService.osInfo
}
