package app.vercors.feature.config.domain

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.runBlocking
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.mapFailure
import opensavvy.state.outcome.value
import kotlin.time.Duration

@ContributesTo(AppScope::class)
interface ConfigurationDomainProviders {
    @Provides
    fun provideConfiguration(observeConfiguration: ObserveConfigurationUseCase): Configuration =
        runBlocking { observeConfiguration().now().mapFailure { error("Unable to load configuration: $it") }.value }

    @Provides
    @Named("metaApiUrl")
    fun provideMetaUrl(configuration: Configuration): String = configuration.metaApiUrl

    @Provides
    @Named("networkTimeout")
    fun provideNetworkTimeout(configuration: Configuration): Duration = configuration.networkTimeout

    @Provides
    @Named("parallelFilesystemOperations")
    fun provideParallelFilesystemOperations(configuration: Configuration): Int =
        configuration.parallelFilesystemOperations
}
