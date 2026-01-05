package app.vercors.feature.account.data

import dev.zacsweers.metro.*

@ContributesTo(AppScope::class)
interface AccountDataProviders {
    @SingleIn(AppScope::class)
    @Named("microsoftClientId")
    @Provides
    fun provideMicrosoftClientId(): String = MICROSOFT_CLIENT_ID
}
