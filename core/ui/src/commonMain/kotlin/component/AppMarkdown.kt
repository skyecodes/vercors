package app.vercors.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.theme.*
import com.composeunstyled.theme.Theme
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.markdownPadding

@Composable
fun AppMarkdown(markdown: String, modifier: Modifier = Modifier) {
    val body = Theme[textStyles][default].copy(fontSize = 12.sp)
    val heading = Theme[textStyles][subtitle]

    Markdown(
        content = markdown,
        colors = DefaultMarkdownColors(
            text = Theme[colors][onBackground],
            codeBackground = Theme[colors][surface2],
            inlineCodeBackground = Theme[colors][surface2],
            dividerColor = Theme[colors][surface2],
            tableBackground = Theme[colors][surface2],
            highlightText = Theme[colors][onBackground],
            highlightBackground = Theme[colors][primary].copy(alpha = 0.2f),
        ),
        typography = DefaultMarkdownTypography(
            h1 = Theme[textStyles][title],
            h2 = heading,
            h3 = heading.copy(fontSize = 16.sp),
            h4 = heading.copy(fontSize = 14.sp),
            h5 = heading.copy(fontSize = 12.sp),
            h6 = heading.copy(fontSize = 10.sp),
            text = body,
            code = Theme[textStyles][console],
            inlineCode = Theme[textStyles][console],
            quote = body,
            paragraph = body,
            ordered = body,
            bullet = body,
            list = body,
            textLink = TextLinkStyles(
                style = SpanStyle(color = Theme[colors][primary], textDecoration = TextDecoration.Underline)
            ),
            table = body,
        ),
        modifier = modifier,
        padding = markdownPadding(
            block = 4.dp
        ),
        imageTransformer = Coil3ImageTransformerImpl,
    )
}
