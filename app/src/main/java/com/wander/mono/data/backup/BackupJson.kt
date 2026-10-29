package com.wander.mono.data.backup

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Between a backup's tree (see [BackupFormat]) and JSON text, with the platform's `org.json`.
 * Kept apart from [BackupFormat] because `org.json` is only a stub in local unit tests.
 */
internal object BackupJson {
    private const val INDENT = 2

    fun write(tree: Map<String, Any>): String = JSONObject(tree).toString(INDENT)

    /** The tree of [text]; null when it is not a JSON object. */
    fun read(text: String): Map<String, Any?>? = try {
        objectToMap(JSONObject(text))
    } catch (_: JSONException) {
        null
    }

    private fun objectToMap(json: JSONObject): Map<String, Any?> =
        json.keys().asSequence().associateWith { plain(json.get(it)) }

    private fun plain(value: Any?): Any? = when (value) {
        is JSONObject -> objectToMap(value)
        is JSONArray -> List(value.length()) { plain(value.get(it)) }
        JSONObject.NULL -> null
        else -> value
    }
}
