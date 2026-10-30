package com.wander.mono.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.core.security.LaunchRequest
import com.wander.mono.ui.components.rememberCredentialPrompt

/**
 * Asks for the screen lock before a locked app opens, from home, the drawer, search or a sheet
 * alike, then opens it as it was asked to. Draws nothing: the prompt is the system's. Cancelling
 * leaves the app closed; the next launch asks again.
 */
@Composable
fun AppLockGate(gate: AppLockViewModel = viewModel()) {
    var waiting by remember { mutableStateOf<LaunchRequest?>(null) }
    val prompt = rememberCredentialPrompt(
        R.string.app_lock_prompt_title,
        onSuccess = {
            waiting?.let(gate::unlockAndLaunch)
            waiting = null
        },
        onNoScreenLock = gate::reportNoScreenLock,
    )
    val ask by rememberUpdatedState(prompt)
    LaunchedEffect(gate) {
        gate.requests.collect { request ->
            if (gate.mustConfirm(request)) {
                waiting = request
                ask()
            } else {
                gate.launch(request)
            }
        }
    }
}
