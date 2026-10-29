package com.wander.mono.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wander.mono.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A bundled font and the licence it ships under. */
private class FontLicence(@StringRes val name: Int, @RawRes val text: Int)

private val fontLicences = listOf(
    FontLicence(R.string.about_font_google_sans_flex, R.raw.ofl_google_sans_flex),
    FontLicence(R.string.about_font_nunito, R.raw.ofl_nunito),
    FontLicence(R.string.about_font_dm_serif_display, R.raw.ofl_dm_serif_display),
    FontLicence(R.string.about_font_jetbrains_mono, R.raw.ofl_jetbrains_mono),
    FontLicence(R.string.about_font_oswald, R.raw.ofl_oswald),
)

/** The version, and the licences of the fonts Mono bundles, each opened in full on a tap. */
@Composable
fun AboutSection() {
    val context = LocalContext.current
    val version = remember(context) { versionName(context) }
    var open by rememberSaveable { mutableStateOf<Int?>(null) }
    SettingsGroup(R.string.settings_about) {
        ListItem(
            colors = settingRowColors(),
            leadingContent = leadingBadge(R.drawable.sym_info),
            supportingContent = { Text(version) },
        ) {
            Text(stringResource(R.string.settings_about_version))
        }
    }
    SettingsGroup(R.string.settings_about_licences) {
        fontLicences.forEachIndexed { index, licence ->
            ActionRow(
                title = licence.name,
                summary = R.string.settings_about_ofl,
                icon = R.drawable.sym_license,
                onClick = { open = index },
            )
        }
    }
    open?.let { index -> LicenceDialog(fontLicences[index], onDismiss = { open = null }) }
}

/** Mono's own version; its own package is always there to ask. */
private fun versionName(context: Context): String {
    val manager = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        manager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        manager.getPackageInfo(context.packageName, 0)
    }
    return info.versionName.orEmpty()
}

@Composable
private fun LicenceDialog(licence: FontLicence, onDismiss: () -> Unit) {
    val resources = LocalContext.current.resources
    val text by produceState<String?>(null, licence) {
        value = withContext(Dispatchers.IO) {
            resources.openRawResource(licence.text).bufferedReader().use { it.readText() }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.about_licence_close)) } },
        title = { Text(stringResource(licence.name)) },
        text = {
            Text(
                text.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
            )
        },
    )
}
