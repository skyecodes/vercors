package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.game.domain.repository.GameRepository
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.model.options.*
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.state.arrow.toEither
import opensavvy.state.coroutines.now

private val logger = KotlinLogging.logger {}

@Inject
class CopyOptionsUseCase(
    private val instanceRepository: InstanceRepository,
    private val instanceOptionsRepository: InstanceOptionsRepository,
    private val gameRepository: GameRepository,
) {
    context(_: Raise<InstanceOptionsError>)
    suspend operator fun invoke(
        source: OptionsSource,
        targetId: InstanceId,
        categories: Set<OptionsCategory>,
        overwriteFiles: Boolean = true,
    ): OptionsCopyResult {
        val target = withError({ InstanceOptionsError.InstanceNotFound(it.id) }) {
            instanceRepository.findInstance(targetId)
        }
        // The game rewrites options.txt when it exits, which would silently undo the copy
        val status = instanceRepository.getInstanceStatus(targetId)
        ensure(status !is InstanceStatus.Running && status !is InstanceStatus.Stopping) {
            InstanceOptionsError.InstanceRunning(targetId)
        }

        val targetSource = OptionsSource.Instance(targetId)
        val skipped = mutableSetOf<OptionsCategory>()
        instanceOptionsRepository.readOptions(source)?.let { sourceOptions ->
            val targetOptions = instanceOptionsRepository.readOptions(targetSource)
            if (OptionsCategory.Keybinds in categories && !keybindsCompatible(sourceOptions, targetOptions, target)) {
                skipped += OptionsCategory.Keybinds
            }
            val optionCategories = categories - FILE_OPTIONS_CATEGORIES - skipped
            if (optionCategories.isNotEmpty()) {
                instanceOptionsRepository.writeOptions(
                    targetSource,
                    targetOptions.mergeFrom(sourceOptions, optionCategories)
                )
            }
            if (OptionsCategory.ResourcePacks in optionCategories) {
                instanceOptionsRepository.copyResourcePacks(source, targetSource, sourceOptions)
            }
        }
        if (OptionsCategory.ServerList in categories) {
            instanceOptionsRepository.copyFile(source, targetSource, OptionsFile.ServersDat, overwriteFiles)
        }
        if (OptionsCategory.Hotbars in categories) {
            instanceOptionsRepository.copyFile(source, targetSource, OptionsFile.HotbarNbt, overwriteFiles)
        }
        logger.info { "Copied options $categories from $source to $targetId (skipped: $skipped)" }
        return OptionsCopyResult(skipped)
    }

    private suspend fun keybindsCompatible(source: GameOptions, target: GameOptions?, instance: Instance): Boolean {
        val sourceFormat = source.keybindFormat() ?: return true
        val targetFormat = target?.keybindFormat() ?: gameVersionKeybindFormat(instance.gameVersion) ?: return false
        return sourceFormat == targetFormat
    }

    private suspend fun gameVersionKeybindFormat(gameVersion: String): KeybindFormat? =
        gameRepository.observeVersionManifest().now().toEither().getOrNull()
            ?.versions
            ?.find { it.id == gameVersion }
            ?.let { if (it.releaseTime >= MODERN_KEYBINDS_SINCE) KeybindFormat.Modern else KeybindFormat.Legacy }
}
