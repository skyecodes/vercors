package app.vercors.website.pages.blog

import androidx.compose.runtime.Composable
import app.vercors.website.CardStyle
import app.vercors.website.HeadlineTextStyle
import app.vercors.website.SiteColors
import app.vercors.website.blog.BLOG_POSTS
import app.vercors.website.blog.BlogPost
import app.vercors.website.components.layouts.PageLayoutData
import app.vercors.website.formatDate
import com.varabyte.kobweb.compose.css.autoLength
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UncoloredLinkVariant
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2

@InitRoute
fun initBlogPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Blog"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun BlogPage() {
    Column(Modifier.fillMaxWidth().maxWidth(48.cssRem).margin(leftRight = autoLength).gap(1.cssRem)) {
        H1(HeadlineTextStyle.toModifier().margin(top = 0.cssRem, bottom = 1.cssRem).toAttrs()) { SpanText("Blog") }
        if (BLOG_POSTS.isEmpty()) {
            SpanText("No posts yet.", Modifier.color(SiteColors.subtext))
        }
        BLOG_POSTS.forEach { BlogPostCard(it) }
    }
}

@Composable
private fun BlogPostCard(post: BlogPost) {
    Link(post.route, Modifier.fillMaxWidth(), variant = UndecoratedLinkVariant.then(UncoloredLinkVariant)) {
        Column(CardStyle.toModifier().fillMaxWidth().gap(0.4.cssRem)) {
            SpanText(formatDate(post.date), Modifier.fontSize(0.85.cssRem).color(SiteColors.subtext))
            H2(
                Modifier.margin(0.cssRem).fontSize(1.4.cssRem).color(SiteColors.primary).toAttrs()
            ) { SpanText(post.title) }
            SpanText(post.description)
        }
    }
}
