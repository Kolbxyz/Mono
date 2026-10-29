package com.wander.mono.ui.drawer

/** What the private space header in the drawer can do: lock or unlock it, open its settings. */
class PrivateSpaceActions(
    val onSetLocked: (Boolean) -> Unit,
    val onOpenSettings: () -> Unit,
)
