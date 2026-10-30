package com.wander.mono.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier

/**
 * One category's page as list content, the same on one pane and two: its preview ([pinnedOf]),
 * held at the top of the list (under the title bar, which the list never runs beneath), then its
 * sections ([sectionsOf]). The preview sits on the page's own surface, so rows scroll under it
 * unseen.
 */
internal fun LazyListScope.categoryPage(category: SettingsCategory, settings: SettingsViewModel) {
    pinnedOf(category)?.let { pinned ->
        stickyHeader(key = "${category.name}:pinned") {
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) { pinned(settings) }
        }
    }
    val sections = sectionsOf(category)
    // A section's place on its page is its identity: the list is fixed per category.
    items(sections.size, key = { "${category.name}:$it" }) { sections[it](settings) }
}
