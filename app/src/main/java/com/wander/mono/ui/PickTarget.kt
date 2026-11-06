package com.wander.mono.ui

/** What an app picked in the drawer is for, while the drawer is open to pick one. */
enum class PickTarget {
    /** A new favourite on home, from edit mode. */
    Favorite,

    /** What home's round button opens instead of search, from a long press on it. */
    HomeButton,
}
