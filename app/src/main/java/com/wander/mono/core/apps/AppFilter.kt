package com.wander.mono.core.apps

import org.xmlpull.v1.XmlPullParser

/**
 * The activity in an appfilter `component` attribute, `ComponentInfo{package/class}`; a class
 * starting with a dot is relative to the package. Null for anything else (packs also list
 * `:CALENDAR`-style placeholders and typos).
 */
fun parseComponentInfo(value: String?): PackComponent? {
    val inner = value?.trim()
        ?.takeIf { it.startsWith(COMPONENT_PREFIX) && it.endsWith(COMPONENT_SUFFIX) }
        ?.removePrefix(COMPONENT_PREFIX)?.removeSuffix(COMPONENT_SUFFIX)
    val packageName = inner?.substringBefore('/', missingDelimiterValue = "")?.trim().orEmpty()
    val relative = inner?.substringAfter('/', missingDelimiterValue = "")?.trim().orEmpty()
    return if (packageName.isEmpty() || relative.isEmpty()) {
        null
    } else {
        PackComponent(packageName, if (relative.startsWith('.')) packageName + relative else relative)
    }
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
