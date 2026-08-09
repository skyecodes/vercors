package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface SystemProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideOsInfo(systemService: SystemService): OsInfo = systemService.osInfo
}
