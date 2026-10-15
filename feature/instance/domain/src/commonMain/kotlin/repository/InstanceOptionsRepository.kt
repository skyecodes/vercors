package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.options.*
import arrow.core.raise.Raise
import kotlinx.coroutines.flow.Flow

interface InstanceOptionsRepository {
    fun observeDefaultOptions(): Flow<DefaultOptions?>

    suspend fun getDefaultOptions(): DefaultOptions?

    context(_: Raise<InstanceOptionsError>)
    suspend fun readOptions(source: OptionsSource): GameOptions?

    context(_: Raise<InstanceOptionsError>)
    suspend fun writeOptions(target: OptionsSource, options: GameOptions)

    context(_: Raise<InstanceOptionsError>)
    suspend fun copyFile(from: OptionsSource, to: OptionsSource, file: OptionsFile, overwrite: Boolean)

    // Copies the pack files an options.txt `resourcePacks` value references, never overwriting existing ones
    context(_: Raise<InstanceOptionsError>)
    suspend fun copyResourcePacks(from: OptionsSource, to: OptionsSource, options: GameOptions)

    context(_: Raise<InstanceOptionsError>)
    suspend fun saveDefaultOptions(defaultOptions: DefaultOptions)

    context(_: Raise<InstanceOptionsError>)
    suspend fun clearDefaultOptions()
}
