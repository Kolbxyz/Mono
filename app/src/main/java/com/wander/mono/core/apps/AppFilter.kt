package com.wander.mono.core.apps

import org.xmlpull.v1.XmlPullParser

/** An activity an icon pack draws an icon for, as its `appfilter.xml` names it. */
data class PackComponent(val packageName: String, val className: String)

/**
 * The activity in an appfilter `component` attribute, `ComponentInfo{package/class}`; a class
 * starting with a dot is relative to the package. Null for anything else (packs also list
 * `:CALENDAR`-style placeholders and typos).
 */
fun parseComponentInfo(value: String?): PackComponent? {
    val text = value?.trim() ?: return null
    if (!text.startsWith(COMPONENT_PREFIX) || !text.endsWith(COMPONENT_SUFFIX)) return null
    val inner = text.substring(COMPONENT_PREFIX.length, text.length - COMPONENT_SUFFIX.length)
    val slash = inner.indexOf('/')
    if (slash <= 0 || slash == inner.lastIndex) return null
    val packageName = inner.substring(0, slash).trim()
    val relative = inner.substring(slash + 1).trim()
    if (packageName.isEmpty() || relative.isEmpty()) return null
    val className = if (relative.startsWith('.')) packageName + relative else relative
    return PackComponent(packageName, className)
}

/**
 * The drawable each activity maps to, from `(component, drawable)` attribute pairs of the pack's
 * `<item>` tags. The first mapping of an activity wins, as in the launchers packs are made for.
 */
fun appFilterMap(items: Sequence<Pair<String?, String?>>): Map<PackComponent, String> {
    val map = LinkedHashMap<PackComponent, String>()
    for ((component, drawable) in items) {
        val key = parseComponentInfo(component) ?: continue
        val name = drawable?.trim().orEmpty()
        if (name.isNotEmpty() && key !in map) map[key] = name
    }
    return map
}

/** The `<item component=… drawable=…/>` pairs of an appfilter document, read lazily from this parser. */
fun XmlPullParser.appFilterItems(): Sequence<Pair<String?, String?>> = sequence {
    var event = eventType
    while (event != XmlPullParser.END_DOCUMENT) {
        if (event == XmlPullParser.START_TAG && name == ITEM_TAG) {
            yield(getAttributeValue(null, "component") to getAttributeValue(null, "drawable"))
        }
        event = next()
    }
}

private const val COMPONENT_PREFIX = "ComponentInfo{"
private const val COMPONENT_SUFFIX = "}"
private const val ITEM_TAG = "item"
