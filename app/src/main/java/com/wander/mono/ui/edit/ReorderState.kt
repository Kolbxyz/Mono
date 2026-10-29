package com.wander.mono.ui.edit

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wander.mono.data.moved

/**
 * A drag of one row of a `LazyColumn` among its siblings, by item key. While a drag runs, and
 * until the saved order catches up after the drop, [order] overrides the list's own order.
 * The dragged row follows the finger: it is drawn [displacement] away from where the list placed it.
 */
@Stable
class ReorderState(private val list: LazyListState) {
    var dragging: Any? by mutableStateOf(null)
        private set

    var order: List<Any>? by mutableStateOf(null)
        private set

    private var dragged by mutableFloatStateOf(0f)
    private var startOffset = 0

    /** How far the dragged row is drawn from its laid-out place, so it stays under the finger. */
    val displacement: Float
        get() = itemInfo(dragging)?.let { startOffset + dragged - it.offset } ?: 0f

    fun start(key: Any, current: List<Any>) {
        order = current
        dragging = key
        dragged = 0f
        startOffset = itemInfo(key)?.offset ?: 0
    }

    /** Follows the finger by [dy]; true when the row took a new slot. */
    fun dragBy(dy: Float): Boolean {
        dragged += dy
        val key = dragging ?: return false
        val current = order ?: return false
        val info = itemInfo(key) ?: return false
        val middle = (startOffset + dragged + info.size / 2f).toInt()
        val target = list.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key != key && it.key in current && middle in it.offset until it.offset + it.size
        } ?: return false
        order = current.moved(key, current.indexOf(target.key) - current.indexOf(key))
        return true
    }

    /** How far the dragged row reaches past the top (negative) or bottom of the viewport. */
    fun overflow(): Float {
        val info = itemInfo(dragging) ?: return 0f
        val top = startOffset + dragged
        val bottom = top + info.size
        val layout = list.layoutInfo
        return when {
            top < layout.viewportStartOffset -> top - layout.viewportStartOffset
            bottom > layout.viewportEndOffset -> bottom - layout.viewportEndOffset
            else -> 0f
        }
    }

    /** Ends the drag and returns the order to save. */
    fun drop(): List<Any>? {
        dragging = null
        dragged = 0f
        return order
    }

    /** Drops the override once the list's own order matches it (saved), or no drag runs. */
    fun settle(source: List<Any>) {
        if (dragging == null && order != null && order == source) order = null
    }

    /** Drops the override without waiting, when edit mode ends. */
    fun reset() {
        dragging = null
        order = null
    }

    private fun itemInfo(key: Any?): LazyListItemInfo? =
        key?.let { k -> list.layoutInfo.visibleItemsInfo.firstOrNull { it.key == k } }
}
