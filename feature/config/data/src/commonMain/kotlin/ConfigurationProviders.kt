package app.vercors.feature.config.data

import app.vercors.core.domain.AppConstants
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import dev.zacsweers.metro.*
import kotlinx.coroutines.runBlocking
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.mapFailure
import opensavvy.state.outcome.value
import kotlin.time.Duration

@ContributesTo(AppScope::class)
@BindingContainer
object ConfigurationProviders {
    @Provides
    fun provideConfiguration(observeConfiguration: ObserveConfigurationUseCase): Configuration =
        runBlocking { observeConfiguration().now().mapFailure { error("Unable to load configuration: $it") }.value }

    @Provides
    @Named(AppConstants.META_API_URL_KEY)
    fun provideMetaUrl(configuration: Configuration): String = configuration.metaApiUrl

    @Provides
    @Named(AppConstants.NETWORK_TIMEOUT_KEY)
    fun provideNetworkTimeout(configuration: Configuration): Duration = configuration.networkTimeout

    @Provides
    @Named(AppConstants.PARALLEL_FILESYSTEM_OPERATIONS_KEY)
    fun provideParallelFilesystemOperations(configuration: Configuration): Int =
        configuration.parallelFilesystemOperations
}
