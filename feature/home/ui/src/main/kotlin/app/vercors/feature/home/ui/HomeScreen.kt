package app.vercors.feature.home.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
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
import app.vercors.core.ui.animation.appAnimateContentSize
import app.vercors.core.ui.animation.appAnimateDpAsState
import app.vercors.core.ui.animation.shouldAnimate
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.back
import app.vercors.shared.ui.create
import app.vercors.shared.ui.next
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.launch
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
            is HomeAction.ShowErrorPopup -> Unit
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
        HomeState.Loading -> AppLoading()
        is HomeState.Loaded -> Column {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
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
private fun <T> HomeSectionDisplay(section: HomeSectionView<T>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        AppText(
            text = stringResource(section.type.string),
            style = Theme[textStyles][title],
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (section.type == HomeSectionType.RecentlyPlayed && section.data.isNullOrEmpty()) {
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
private fun <T> HomeSectionList(
    section: HomeSectionView<T>
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val shouldAnimate = shouldAnimate()
    val onScrollButtonClick: (Float) -> Unit = {
        coroutineScope.launch { if (shouldAnimate) listState.animateScrollBy(it) else listState.scrollBy(it) }
    }

    Box {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
        ) {
            val data = section.data
            data?.forEachIndexed { index, element ->
                homeCardItem(
                    data = data,
                    index = index,
                    element = element
                )
            } ?: repeat(10) {
                fakeCardItem(
                    index = it,
                    maxIndex = 9,
                    sectionType = section.type
                )
            }
        }

        if (listState.canScrollForward) {
            HomeScrollButton(
                onClick = onScrollButtonClick,
                isRight = true
            )
        }
        if (listState.canScrollBackward) {
            HomeScrollButton(
                onClick = onScrollButtonClick,
                isRight = false
            )
        }
    }
}

context(
    store: IntentReceiver<HomeIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun <T> LazyListScope.homeCardItem(
    data: List<T>,
    index: Int,
    element: T
) {
    val modifier = Modifier.padding(
        start = if (index == 0) 8.dp else 0.dp,
        end = if (index == data.lastIndex) 8.dp else 0.dp
    )

    when (element) {
        is BriefInstanceView -> item(element.id) {
            BriefInstanceCard(
                instance = element,
                onInstanceClick = { store.intent(HomeIntent.OnInstanceClick(it)) },
                modifier = modifier.size(width = 150.dp, height = 150.dp),
                origin = InstanceSharedElementOrigin.Home
            )
        }

        is BriefProjectView -> item(element.id) {
            HomeProjectCard(
                project = element,
                modifier = modifier
            )
        }
    }
}

context(
    _: IntentReceiver<HomeIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun LazyListScope.fakeCardItem(
    index: Int,
    maxIndex: Int,
    sectionType: HomeSectionType
) {
    val modifier = Modifier.padding(
        start = if (index == 0) 8.dp else 0.dp,
        end = if (index == maxIndex) 8.dp else 0.dp
    )

    item(sectionType to index) {
        when (sectionType) {
            HomeSectionType.RecentlyPlayed -> BriefInstanceCard(
                instance = null,
                onInstanceClick = {},
                modifier = modifier.size(width = 150.dp, height = 150.dp),
                origin = InstanceSharedElementOrigin.Home
            )

            else -> HomeProjectCard(
                project = null,
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
            contentDescription = if (isRight) stringResource(SharedRes.string.next) else stringResource(SharedRes.string.back),
        )
    }
}

@Composable
context(store: IntentReceiver<HomeIntent>)
private fun NoInstanceFoundDisplay() {
    Column(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = stringResource(InstanceRes.string.noInstanceFound)
        )

        AppIconTextButton(
            onClick = { store.intent(HomeIntent.OnCreateInstanceClick) },
            text = stringResource(SharedRes.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        )
    }
}

@Composable
context(
    store: IntentReceiver<HomeIntent>,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
private fun HomeProjectCard(
    project: BriefProjectView?,
    modifier: Modifier
) = with(sharedTransitionScope) {
    if (project != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        HomeProjectCardLayout(
            modifier = modifier
                .hoverable(interactionSource = interactionSource)
                .projectSharedElement(project.id, "card", ProjectSharedElementOrigin.Home),
            interactionSource = interactionSource,
            onClick = { store.intent(HomeIntent.OnProjectClick(project.id)) },
            icon = {
                Image(
                    painter = rememberAsyncImagePainter(project.logoUrl),
                    contentDescription = project.name,
                    modifier = it
                        .projectSharedElement(project.id, "logo", ProjectSharedElementOrigin.Home)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            title = {
                AppText(
                    text = project.name,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .projectSharedElement(project.id, "title", ProjectSharedElementOrigin.Home)
                )
            },
            author = {
                AppText(
                    text = "by ${project.author}",
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            image = {
                val blurRadius by appAnimateDpAsState(if (isHovered) 4.dp else 0.dp)
                val projectImageModifier = Modifier.fillMaxSize().blur(blurRadius)

                project.imageUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = project.name,
                        modifier = projectImageModifier,
                        contentScale = ContentScale.Crop
                    )
                } ?: Image(
                    bitmap = imageResource(ProjectRes.drawable.pack),
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
                        .appAnimateContentSize()
                            then if (isHovered) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(0.dp)
                ) {
                    AppText(
                        text = project.description,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                            .projectSharedElement(project.id, "description", ProjectSharedElementOrigin.Home),
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HomeProjectChip(
                        icon = Lucide.Download,
                        text = project.downloadsReadable
                    )
                    HomeProjectChip(
                        icon = Lucide.Calendar,
                        text = project.lastUpdatedReadable
                    )
                }
            }
        )
    } else {
        HomeProjectCardLayout(
            modifier = modifier,
            icon = { AppLoadingPlaceholder(it) },
            title = { AppLoadingTextPlaceholder(Modifier.width(150.dp)) },
            author = { AppLoadingTextPlaceholder(Modifier.width(100.dp)) },
            image = { AppLoadingPlaceholder(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun HomeProjectCardLayout(
    modifier: Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    icon: @Composable context(AnimatedContentScope, SharedTransitionScope) RowScope.(Modifier) -> Unit,
    title: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    author: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    image: @Composable context(AnimatedContentScope, SharedTransitionScope) BoxScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .appCard(interactionSource = interactionSource, onClick = onClick)
            .size(width = 250.dp, height = 180.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon(Modifier.size(36.dp))
            Column {
                title()
                author()
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            image()
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
            contentDescription = null,
            modifier = Modifier.size(12.dp)
        )
        AppText(
            text = text,
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
