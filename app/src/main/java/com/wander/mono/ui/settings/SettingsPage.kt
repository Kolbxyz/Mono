package com.wander.mono.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import com.wander.mono.ui.components.readableColumn

/** One settings page: a collapsing title with a back arrow over a list of [content]. */
@Composable
internal fun SettingsPage(
    @StringRes title: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scroll.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.sym_arrow_back), stringResource(R.string.action_back))
                    }
                },
                scrollBehavior = scroll,
                // safeDrawing, not the default system bars: a landscape cutout must not cover the back arrow.
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            )
        },
        // The top bar pads the status bar itself; the list pads the bottom, the sides come below.
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
    ) { padding ->
        // The bar spans the window; the rows sit in the centred readable column, inside the
        // side insets so a landscape cutout pushes the column only when it reaches it. The list
        // starts under the bar rather than scrolling beneath it: a pinned preview ([categoryPage])
        // sticks to the list's top edge, not to its content padding.
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .readableColumn(),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + PageEndSpace),
            content = content,
        )
    }
}

/** Room under a page's last group, so it ends on a margin rather than at the window's edge. */
internal val PageEndSpace = 32.dp
