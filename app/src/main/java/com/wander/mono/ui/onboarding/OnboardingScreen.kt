package com.wander.mono.ui.onboarding

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wander.mono.R
import com.wander.mono.ui.components.readableColumn

/**
 * First-run setup over home, until finished or skipped. Until it is known whether setup was done,
 * a plain surface covers home, so a first run never shows home a moment before setup; the cover
 * fades out on every later start, as setup itself does when finished.
 */
@Composable
fun Onboarding(onboarding: OnboardingViewModel = viewModel()) {
    val done by onboarding.done.collectAsStateWithLifecycle()
    Crossfade(done, animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(), label = "onboarding") {
        when (it) {
            null -> Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {}
            false -> OnboardingPanes(onboarding)
            true -> Unit
        }
    }
}

/**
 * The setup steps, one per pane, each with one main action and Skip; text sits low, near the
 * thumb, like the home list.
 */
@Composable
private fun OnboardingPanes(onboarding: OnboardingViewModel) {
    val ui by onboarding.ui.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme
    val homeRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onboarding.recheck()
    }

    LifecycleStartEffect(onboarding) {
        onboarding.recheck()
        onStopOrDispose {}
    }
    BackHandler(enabled = ui.canGoBack, onBack = onboarding::back)

    fun requestHome() {
        try {
            homeRequest.launch(onboarding.homeRequestIntent())
        } catch (_: ActivityNotFoundException) {
            onboarding.onHomeRequestMissing()
        }
    }

    val primary: () -> Unit = when (ui.step) {
        OnboardingStep.DefaultHome -> ::requestHome
        OnboardingStep.NotificationAccess -> onboarding::openAccessSettings
        OnboardingStep.Welcome, OnboardingStep.Done -> onboarding::next
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .readableColumn()
                .padding(24.dp),
        ) {
            AnimatedContent(
                targetState = ui.step,
                modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
                label = "onboarding step",
            ) { step ->
                StepBody(step, ui.showRestrictedHelp, onboarding::openAppInfo)
            }
            // The error grows in and out instead of pushing the buttons down in one frame.
            AnimatedContent(
                targetState = ui.error,
                transitionSpec = {
                    (fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()))
                        .using(SizeTransform { _, _ -> motion.defaultSpatialSpec() })
                },
                label = "onboarding error",
            ) { error ->
                if (error != null) {
                    Text(
                        stringResource(error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            // Wraps onto two lines at large font sizes rather than pushing the main action off screen.
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (ui.step != OnboardingStep.Done) {
                    TextButton(onClick = onboarding::next) { ButtonLabel(R.string.onboarding_skip) }
                } else {
                    Spacer(Modifier)
                }
                Button(onClick = primary) { ButtonLabel(primaryLabel(ui.step, ui.showRestrictedHelp)) }
            }
        }
    }
}

@Composable
private fun ButtonLabel(@StringRes label: Int) {
    Text(stringResource(label), maxLines = 2, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun StepBody(step: OnboardingStep, showRestrictedHelp: Boolean, onOpenAppInfo: () -> Unit) {
    // Short steps sit at the bottom; a long one (large font, the restricted-settings card) scrolls.
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(step.title),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(step.body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (step == OnboardingStep.NotificationAccess && showRestrictedHelp) {
                RestrictedSettingsCard(onOpenAppInfo)
            }
        }
    }
}

@StringRes
private fun primaryLabel(step: OnboardingStep, retry: Boolean): Int = when (step) {
    OnboardingStep.Welcome -> R.string.onboarding_continue
    OnboardingStep.DefaultHome -> R.string.onboarding_home_action
    OnboardingStep.NotificationAccess ->
        if (retry) R.string.onboarding_access_retry else R.string.onboarding_access_action
    OnboardingStep.Done -> R.string.onboarding_done_action
}

private val OnboardingStep.title: Int
    @StringRes get() = when (this) {
        OnboardingStep.Welcome -> R.string.onboarding_welcome_title
        OnboardingStep.DefaultHome -> R.string.onboarding_home_title
        OnboardingStep.NotificationAccess -> R.string.onboarding_access_title
        OnboardingStep.Done -> R.string.onboarding_done_title
    }

private val OnboardingStep.body: Int
    @StringRes get() = when (this) {
        OnboardingStep.Welcome -> R.string.onboarding_welcome_body
        OnboardingStep.DefaultHome -> R.string.onboarding_home_body
        OnboardingStep.NotificationAccess -> R.string.onboarding_access_body
        OnboardingStep.Done -> R.string.onboarding_done_body
    }
