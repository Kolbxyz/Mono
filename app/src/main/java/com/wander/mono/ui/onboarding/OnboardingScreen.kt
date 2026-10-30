package com.wander.mono.ui.onboarding

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.wander.mono.core.calendar.CalendarAccess
import com.wander.mono.ui.components.readableColumn

/**
 * First-run setup over home, until finished or skipped. Until it is known whether setup was done
 * nothing is drawn: covering home then would flash a surface on every cold start, for a read that
 * takes a few milliseconds. Setup appears at once when the read finds it not done, and fades out
 * when finished.
 */
@Composable
fun Onboarding(onboarding: OnboardingViewModel = viewModel()) {
    val done by onboarding.done.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = done,
        transitionSpec = {
            val fade = if (initialState == null) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.defaultEffectsSpec())
            }
            // No size animation: setup fills the screen and leaves nothing behind it.
            fade.using(null)
        },
        label = "onboarding",
    ) {
        if (it == false) OnboardingPanes(onboarding)
    }
}

/**
 * The setup steps, one per pane, each with one main action and Skip; text sits low, near the
 * thumb, like the home list.
 */
@Composable
private fun OnboardingPanes(
    onboarding: OnboardingViewModel,
    picker: FavoritePickerViewModel = viewModel(),
) {
    val ui by onboarding.ui.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme
    val homeRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onboarding.recheck()
    }
    val calendarRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onboarding::onCalendarAccess,
    )

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

    fun addFavorites() {
        picker.save()
        onboarding.next()
    }

    // Granted already in App info: nothing to ask, the setting just goes on.
    fun showCalendar() {
        if (onboarding.hasCalendarAccess()) {
            onboarding.onCalendarAccess(true)
        } else {
            calendarRequest.launch(CalendarAccess.PERMISSION)
        }
    }

    val primary: () -> Unit = when (ui.step) {
        OnboardingStep.DefaultHome -> ::requestHome
        OnboardingStep.Favorites -> ::addFavorites
        OnboardingStep.NotificationAccess -> onboarding::openAccessSettings
        OnboardingStep.Calendar -> ::showCalendar
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
                if (step == OnboardingStep.Favorites) {
                    FavoritePicker(picker)
                } else {
                    StepBody(step, ui.showRestrictedHelp, onboarding::openAppInfo)
                }
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
            StepBadge(step, Modifier.padding(bottom = 8.dp))
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
