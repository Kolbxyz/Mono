package com.wander.mono.ui.components

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

/**
 * Asks for the screen lock (or a fingerprint, which the system offers next to it) and calls
 * [onSuccess] once confirmed. The platform prompt on Android 11+, the keyguard's confirm screen
 * before. Without a screen lock there is nothing to confirm: [onNoScreenLock] says so.
 * Cancelling does nothing; the system prompt shows its own errors.
 */
@Composable
fun rememberCredentialPrompt(
    @StringRes title: Int,
    onSuccess: () -> Unit,
    onNoScreenLock: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val titleText = stringResource(title)
    val success by rememberUpdatedState(onSuccess)
    val noScreenLock by rememberUpdatedState(onNoScreenLock)
    val confirm = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) success()
    }
    return remember(context, titleText) {
        {
            val keyguard = context.getSystemService(KeyguardManager::class.java)
            when {
                !keyguard.isDeviceSecure -> noScreenLock()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> authenticate(context, titleText) { success() }
                else -> {
                    @Suppress("DEPRECATION")
                    keyguard.createConfirmDeviceCredentialIntent(titleText, null)?.let(confirm::launch)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private fun authenticate(context: Context, title: String, onSuccess: () -> Unit) {
    BiometricPrompt.Builder(context)
        .setTitle(title)
        .setAllowedAuthenticators(Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL)
        .build()
        .authenticate(
            CancellationSignal(),
            context.mainExecutor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            },
        )
}
