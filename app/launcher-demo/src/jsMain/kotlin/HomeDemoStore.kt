package app.vercors.launcher.demo

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.home.ui.HomeEvent
import app.vercors.feature.home.ui.HomeState
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.launcher.demo.data.FEATURED_MODPACKS
import app.vercors.launcher.demo.data.FEATURED_MODS
import app.vercors.launcher.demo.data.recentInstances
import app.vercors.launcher.demo.data.toView
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.overlay.DialogOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import opensavvy.state.progressive.ProgressiveOutcome

// promptCreateInstance stays false, so this is never shown
private object UnusedCreateInstanceDialog : DialogOverlay<CreateInstanceResult>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<CreateInstanceResult>,
        transitionController: OverlayTransitionController,
    ) = Unit
}

fun homeDemoStore(): Store<HomeState, HomeEvent> = Store(
    state = HomeState(
        sections = listOf(
            HomeState.Section.InstanceList(
                type = HomeSectionType.RecentlyPlayed,
                data = ProgressiveOutcome.Success(recentInstances()),
            ),
            HomeState.Section.ProjectList(
                type = HomeSectionType.FeaturedModpacks,
                data = ProgressiveOutcome.Success(FEATURED_MODPACKS.map { it.toView() }),
            ),
            HomeState.Section.ProjectList(
                type = HomeSectionType.FeaturedMods,
                data = ProgressiveOutcome.Success(FEATURED_MODS.map { it.toView() }),
            ),
        ),
        promptCreateInstance = false,
        createInstanceDialog = UnusedCreateInstanceDialog,
        installTarget = null,
        pendingDeleteInstanceId = null,
        copyInstanceDialog = null,
    ),
    eventSink = {},
)
