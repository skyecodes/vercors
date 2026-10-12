package app.vercors.feature.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import app.vercors.feature.project.ui.list.IconView
import app.vercors.feature.project.ui.list.ProjectCategoryView
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.brands.Twitch
import com.composables.icons.lucide.*
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

val IconView.painter: Painter
    @Composable get() = when (this) {
        is IconView.Data -> LocalDensity.current.let {
            remember {
                svgString.encodeToByteArray().decodeToSvgPainter(it)
            }
        }
        is IconView.Named -> rememberVectorPainter(getIconFromName(name))
    }

@Composable
fun categoryText(category: String): String = when (category) {
    "categories" -> stringResource(ProjectRes.string.categories)
    "resolutions" -> stringResource(ProjectRes.string.resolutions)
    "features" -> stringResource(ProjectRes.string.features)
    "performance impact" -> stringResource(ProjectRes.string.performanceImpact)
    else -> category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@Composable
fun translateCategory(category: ProjectCategoryView): String =
    ProjectRes.allStringResources[category.slugSnakeCase]?.let { stringResource(it) } ?: category.name ?: category.slug

private val namedIcons: Map<String, ImageVector> = mapOf(
    "compass" to Lucide.Compass,
    "cloud-sun-rain" to Lucide.CloudSunRain,
    "headphones" to Lucide.Headphones,
    "box" to Lucide.Box,
    "lamp" to Lucide.Lamp,
    "brush" to Lucide.Brush,
    "chart-no-axes-column-increasing" to Lucide.ChartNoAxesColumnIncreasing,
    "blend" to Lucide.Blend,
    "swords" to Lucide.Swords,
    "cpu" to Lucide.Cpu,
    "bug" to Lucide.Bug,
    "house" to Lucide.House,
    "dollar-sign" to Lucide.DollarSign,
    "sun" to Lucide.Sun,
    "wrench" to Lucide.Wrench,
    "wand-sparkles" to Lucide.WandSparkles,
    "tree-deciduous" to Lucide.TreeDeciduous,
    "type" to Lucide.Type,
    "carrot" to Lucide.Carrot,
    "sliders-vertical" to Lucide.SlidersVertical,
    "panels-top-left" to Lucide.PanelsTopLeft,
    "signal-high" to Lucide.SignalHigh,
    "pickaxe" to Lucide.Pickaxe,
    "book" to Lucide.Book,
    "feather" to Lucide.Feather,
    "globe" to Lucide.Globe,
    "signal-low" to Lucide.SignalLow,
    "wand" to Lucide.Wand,
    "server" to Lucide.Server,
    "signal-medium" to Lucide.SignalMedium,
    "award" to Lucide.Award,
    "square-plus" to Lucide.SquarePlus,
    "layers" to Lucide.Layers,
    "users" to Lucide.Users,
    "zap" to Lucide.Zap,
    "lightbulb" to Lucide.Lightbulb,
    "network" to Lucide.Network,
    "image" to Lucide.Image,
    "flip-horizontal-2" to Lucide.FlipHorizontal2,
    "film" to Lucide.Film,
    "mountain" to Lucide.Mountain,
    "hexagon" to Lucide.Hexagon,
    "message-circle" to Lucide.MessageCircle,
    "archive" to Lucide.Archive,
    "hard-drive" to Lucide.HardDrive,
    "pen-tool" to Lucide.PenTool,
    "truck" to Lucide.Truck,
    "square-pen" to Lucide.SquarePen,
    "toolbox" to Lucide.ToolCase,
    "ice-cream-cone" to Lucide.IceCreamCone,
    "earth" to Lucide.Earth,
    "shield" to Lucide.Shield,
    "boxes" to Lucide.Boxes,
    "heart-crack" to Lucide.HeartCrack,
    "map" to Lucide.Map,
    "drone" to Lucide.Drone,
    "bug-off" to Lucide.BugOff,
    "blocks" to Lucide.Blocks,
    "graduation-cap" to Lucide.GraduationCap,
    "briefcase" to Lucide.Briefcase,
    "cable" to Lucide.Cable,
    "hand-fist" to Lucide.HandFist,
    "puzzle" to Lucide.Puzzle,
    "heart" to Lucide.Heart,
    "video" to Lucide.Video,
    "braces" to Lucide.Braces,
    "castle" to Lucide.Castle,
    "cog" to Lucide.Cog,
    "trophy" to Lucide.Trophy,
    "badge-check" to Lucide.BadgeCheck,
    "twitch" to FontAwesome.Brands.Twitch,
)

@Composable
fun getIconFromName(name: String): ImageVector = when (name) {
    "creeper" -> vectorResource(ProjectRes.drawable.creeper)
    "path-tracing" -> vectorResource(ProjectRes.drawable.path_tracing)
    "potato" -> vectorResource(ProjectRes.drawable.potato)
    "sink" -> vectorResource(ProjectRes.drawable.sink)
    "mcreator" -> vectorResource(ProjectRes.drawable.mcreator)
    else -> namedIcons[name] ?: Lucide.CircleQuestionMark
}
