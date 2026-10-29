package com.wander.mono.core.siblings

import android.database.Cursor

/** One row of a sibling's cursor, read by column name: null when the column is missing or null. */
internal interface ProviderRow {
    fun string(column: String): String?

    fun long(column: String): Long?
}

/** The row [cursor] currently points at. */
internal class CursorRow(private val cursor: Cursor) : ProviderRow {
    override fun string(column: String): String? = index(column)?.let(cursor::getString)

    override fun long(column: String): Long? = index(column)?.let(cursor::getLong)

    private fun index(column: String): Int? =
        cursor.getColumnIndex(column).takeIf { it >= 0 && !cursor.isNull(it) }
}

/** Every row [map] makes sense of; rows it rejects (missing required columns) are skipped. */
internal fun <T : Any> Cursor.mapRows(map: (ProviderRow) -> T?): List<T> {
    val row = CursorRow(this)
    return buildList {
        while (moveToNext()) map(row)?.let(::add)
    }
}
