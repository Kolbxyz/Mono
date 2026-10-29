package com.wander.mono.ui.drawer

/** What the drawer does after a search result is tapped (or chosen with the keyboard's Go). */
enum class HitOutcome {
    /** Something opened (an app, a page, a contact): close the drawer. */
    Opened,

    /** Nothing left the drawer (a copied result, or a failure already reported): stay. */
    Stayed,

    /** Mono's own settings page was chosen: open it over the drawer. */
    OpenMonoSettings,
}
