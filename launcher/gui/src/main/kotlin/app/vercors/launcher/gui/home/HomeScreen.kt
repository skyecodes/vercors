package app.vercors.launcher.gui.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.gui.res.*
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.ui.*
import app.vercors.shared.ui.component.AppButton
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.layout.AppError
import app.vercors.shared.ui.modifier.appCard
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.launch
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
fun HomeScreen(
    container: HomeContainer,
    navigateToCreateInstance: () -> Unit,
    navigateToProjectDetails: (ProjectId) -> Unit,
    navigateToInstanceDetails: (InstanceId) -> Unit,
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            HomeAction.NavigateToCreateInstance -> navigateToCreateInstance()
            is HomeAction.NavigateToInstanceDetails -> navigateToInstanceDetails(action.instanceId)
            is HomeAction.NavigateToProjectDetails -> navigateToProjectDetails(action.projectId)
        }
    }

    HomeScreen(state)
}

@Composable
context(
    _: IntentReceiver<HomeIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun HomeScreen(state: HomeState) {
    when (state) {
        HomeState.Loading -> Text("Loading...")
        is HomeState.Loaded -> Column {
            LazyColumn {
                state.sections.forEach { section ->
                    item(key = section.type) {
                        HomeSectionDisplay(section)
                    }
                }
            }
        }

        is HomeState.Error -> AppError(state.throwable)
    }
}

@Composable
context(
    _: IntentReceiver<HomeIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun HomeSectionDisplay(section: HomeSectionView<*>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        AppText(
            text = stringResource(section.type.string),
            style = Theme[textStyles][title],
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (section.type == HomeSectionType.RecentlyPlayed && section.data.isEmpty()) {
            NoInstanceFoundDisplay()
        } else {
            HomeSectionList(section)
        }
    }
}

@Composable
context(
    _: IntentReceiver<HomeIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun HomeSectionList(
    section: HomeSectionView<*>
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            section.data.forEachIndexed { index, element ->
                homeCardItem(
                    section = section,
                    index = index,
                    element = element
                )
            }
        }

        if (listState.canScrollForward) {
            HomeScrollButton(
                onClick = { coroutineScope.launch { listState.animateScrollBy(it) } },
                isRight = true
            )
        }
        if (listState.canScrollBackward) {
            HomeScrollButton(
                onClick = { coroutineScope.launch { listState.animateScrollBy(it) } },
                isRight = false
            )
        }
    }
}

context(
    _: IntentReceiver<HomeIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun LazyListScope.homeCardItem(
    section: HomeSectionView<*>,
    index: Int,
    element: HomeSectionViewElement
) {
    val modifier = Modifier.padding(
        start = if (index == 0) 8.dp else 0.dp,
        end = if (index == section.data.lastIndex) 8.dp else 0.dp
    )

    item(element.id) {
        when (element) {
            is BriefInstanceView -> HomeInstanceCard(
                instance = element,
                modifier = modifier
            )

            is BriefProjectView -> HomeProjectCard(
                project = element,
                modifier = modifier
            )
        }
    }
}

private const val scrollValue = 1000f

@Composable
private fun BoxScope.HomeScrollButton(
    onClick: (Float) -> Unit,
    isRight: Boolean
) {
    AppButton(
        onClick = { onClick(if (isRight) scrollValue else -scrollValue) },
        shape = CircleShape,
        backgroundColor = Theme[colors][surface],
        modifier = Modifier.align(if (isRight) Alignment.CenterEnd else Alignment.CenterStart).padding(8.dp).size(32.dp)
    ) {
        AppIcon(
            imageVector = if (isRight) Lucide.ArrowRight else Lucide.ArrowLeft,
            contentDescription = if (isRight) stringResource(Res.string.next) else stringResource(Res.string.back),
        )
    }
}

context(store: IntentReceiver<HomeIntent>) @Composable
private fun NoInstanceFoundDisplay() {
    Column(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = stringResource(Res.string.noInstanceFound)
        )

        AppButton(
            onClick = { store.intent(HomeIntent.OnCreateInstanceClick) },
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        ) {
            AppIcon(
                imageVector = Lucide.Plus,
                contentDescription = stringResource(Res.string.create)
            )
            AppText(
                text = stringResource(Res.string.create)
            )
        }
    }
}

@Composable
context(store: IntentReceiver<HomeIntent>)
private fun HomeInstanceCard(instance: BriefInstanceView, modifier: Modifier) {

}

@Composable
context(
    store: IntentReceiver<HomeIntent>,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
private fun HomeProjectCard(project: BriefProjectView, modifier: Modifier) = with(sharedTransitionScope) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = modifier
            .appCard(interactionSource = interactionSource) { store.intent(HomeIntent.OnProjectClick(project.id)) }
            .size(width = 250.dp, height = 180.dp)
            .hoverable(interactionSource = interactionSource)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(project.logoUrl),
                contentDescription = project.name,
                modifier = Modifier.size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .sharedElement(
                        sharedContentState = rememberSharedContentState("project/${project.id}/logo"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                contentScale = ContentScale.Crop
            )
            Column {
                AppText(
                    text = project.name,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("project/${project.id}/title"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                AppText(
                    text = "by ${project.author}",
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            val blurRadius by animateDpAsState(if (isHovered) 4.dp else 0.dp)
            val projectImageModifier = Modifier.fillMaxSize().blur(blurRadius).sharedElement(
                sharedContentState = rememberSharedContentState("project/${project.id}/image"),
                animatedVisibilityScope = animatedVisibilityScope
            )

            project.imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = project.name,
                    modifier = projectImageModifier,
                    contentScale = ContentScale.Crop
                )
            } ?: Image(
                bitmap = imageResource(Res.drawable.pack),
                contentDescription = project.name,
                modifier = projectImageModifier,
                contentScale = ContentScale.Crop
            )

            val gradientBackground = arrayOf(
                0.0f to Theme[colors][surface],
                1f to Theme[colors][surface].copy(alpha = 0.7f)
            )

            Column(
                modifier = Modifier.background(Brush.verticalGradient(colorStops = gradientBackground))
                    .animateContentSize()
                        then if (isHovered) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(0.dp)
            ) {
                AppText(
                    text = project.description,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp),
                )
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeProjectChip(
                    icon = Lucide.Download,
                    text = HumanReadable.abbreviation(project.downloads, decimals = 1)
                )
                HomeProjectChip(
                    icon = Lucide.Calendar,
                    text = HumanReadable.timeAgo(project.lastUpdated)
                )
            }
        }
    }
}

@Composable
private fun HomeProjectChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.background(Theme[colors][surface], RoundedCornerShape(4.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = stringResource(Res.string.downloads),
            modifier = Modifier.size(12.dp)
        )
        AppText(
            text = text,
            //text = HumanReadable.abbreviation(project.downloads),
            fontSize = 10.sp,
        )
    }
}

private val HomeSectionType.string: StringResource
    get() = when (this) {
        HomeSectionType.RecentlyPlayed -> Res.string.recentlyPlayed
        HomeSectionType.FeaturedMods -> Res.string.featuredMods
        HomeSectionType.FeaturedModpacks -> Res.string.featuredModpacks
        HomeSectionType.FeaturedResourcePacks -> Res.string.featuredResourcePacks
        HomeSectionType.FeaturedShaderPacks -> Res.string.featuredShaderPacks
    }
