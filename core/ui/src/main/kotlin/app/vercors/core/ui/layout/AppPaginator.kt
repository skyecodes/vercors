package app.vercors.core.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.modifier.clickableText
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.Lucide

@Composable
fun AppPaginator(
    offset: Int,
    pageSize: Int,
    totalElements: Int,
    onOffsetChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalElements > 0) {
        val currentPage = offset / pageSize + 1
        val maxPage = totalElements / pageSize + 1
        val pageText: @Composable (Int) -> Unit = {
            AppText(
                text = "$it",
                modifier = if (it != currentPage) Modifier.clickableText { onOffsetChange((it - 1) * pageSize) } else Modifier,
                fontWeight = if (it == currentPage) FontWeight.Bold else FontWeight.Medium,

                )
        }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppPaginatorButtons(currentPage, onOffsetChange, pageSize, pageText, maxPage)
        }
    }
}

@Composable
private fun RowScope.AppPaginatorButtons(
    currentPage: Int,
    onOffsetChange: (Int) -> Unit,
    pageSize: Int,
    pageText: @Composable (Int) -> Unit,
    maxPage: Int
) {
    if (currentPage > 1) {
        AppIcon(
            imageVector = Lucide.ChevronLeft,
            contentDescription = null,
            modifier = Modifier.size(16.dp).clickableText { onOffsetChange((currentPage - 2) * pageSize) }
        )
    }
    if (currentPage != 1) {
        pageText(1)
    }
    if (currentPage > 3) {
        AppIcon(
            imageVector = Lucide.Ellipsis,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
    if (currentPage > 2) {
        pageText(currentPage - 1)
    }
    pageText(currentPage)
    if (currentPage < maxPage - 1) {
        pageText(currentPage + 1)
    }
    if (currentPage < maxPage - 2) {
        AppIcon(
            imageVector = Lucide.Ellipsis,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
    if (currentPage != maxPage) {
        pageText(maxPage)
    }
    if (currentPage < maxPage) {
        AppIcon(
            imageVector = Lucide.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp).clickableText { onOffsetChange(currentPage * pageSize) }
        )
    }
}
