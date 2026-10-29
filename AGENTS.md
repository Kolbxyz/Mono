# Mono

Minimalist Android launcher in the Niagara style: one vertical list of favourites with live rows (now playing) inline, and an alphabetical drawer with search. Built for GrapheneOS. Strict Material 3 Expressive. Battery- and privacy-first: no network permission, zero telemetry, no Google services.

## 1. Non-Negotiable Hard Rules

| Rule | Why |
| --- | --- |
| **300 lines max per file** | Split when a file reaches 250 lines; 1 concept per file. |
| **No network, ever** | No `INTERNET` permission. A launcher has nothing to fetch; nothing leaves the device. |
| **`LauncherApps` is the app source** | Apps, profiles, shortcuts, launch, app info: always through `LauncherApps`, so work profiles behave. |
| **No polling** | React to `LauncherApps.Callback`, broadcasts and listener callbacks; re-check only on `onStart` what has no event. |
| **Rows, not screens** | The home list is a sealed `HomeRow`; live content (media, widgets later) is a row next to its app. |
| **Strict Material 3 Expressive** | `MaterialExpressiveTheme`, wallpaper dynamic color, motion from `MaterialTheme.motionScheme`, never hand-rolled `spring()`. |
| **Explicit window insets** | Edge-to-edge needs manual container-edge padding. |
| **No Google services** | No Play Services, Firebase or GMS fonts. |
| **Strings in `strings.xml`** | Pass `@StringRes Int`; never hardcode user text in UI. |
| **No dead code & no fake fallbacks** | Surface explicit errors (`ActionResult.Failed`); no blanket `catch (e: Exception)`; never show placeholder data. |
| **Platform APIs before dependencies** | Justify any new dependency and ask approval first. |
| **Never push to `main`** | Feature branch and PR. |
| **No AI attribution** | No `Co-Authored-By` trailer for any AI and no "Generated with ..." line in commits or PR descriptions. |

## 2. Package Map

Single module `:app`, package root `com.wander.mono`.

| Package | Owns |
| --- | --- |
| `core/apps` | `LauncherApps` catalog, profile callbacks, shortcuts, icon cache, launch / app info / uninstall. |
| `core/notifications` | `MonoNotificationListener`, per-app notification counts, notification-access state. |
| `core/media` | Active media sessions (through the listener's grant) and the playing item. |
| `core/calendar` | Opt-in upcoming events (`READ_CALENDAR`) from `CalendarContract.Instances`, observed, re-read only at event boundaries. |
| `core/system` | Clock tick and next alarm, from system broadcasts. |
| `data` | DataStore: ordered favourites and hidden apps. |
| `data/settings` | User settings: `Setting` types, `SettingsStore` (its own DataStore file), one `*Settings.kt` per feature. |
| `di` | Hilt modules and the app coroutine scope. |
| `ui/home` | Glance header, `HomeRow` assembly, favourite / now-playing / prompt rows. |
| `ui/drawer` | Alphabetical list, letter rail, ranked search (apps + shortcuts). |
| `ui/sheet` | Long-press options: per app (`AppSheet`) and for home itself (`HomeSheet`). |
| `ui/settings` | Settings screen, reusable setting rows, one `*Section.kt` per feature. |
| `ui/theme` | `MonoTheme`, reduced motion. |

## 3. Window Insets & Motion

- The window shows the wallpaper; home draws a surface scrim so M3 text colors stay readable.
- Containers pad `WindowInsets.safeDrawing` on the edges they touch (`.only(...)` when only some matter). `safeDrawing` includes the IME.
- Animations take specs from `MaterialTheme.motionScheme`; reduced motion swaps in `NoMotionScheme` (1 ms tweens, not `snap()`).

## 4. Adding a Setting

Settings opens from long-pressing the clock or an empty spot on home (`HomeSheet`) and from the gear in the drawer's search field. It is an overlay driven by `LauncherViewModel.settingsOpen`, like the drawer, and closes on `leaveToHome`.

1. Declare the key in your own `data/settings/<Feature>Settings.kt`: `BooleanSetting(name, default)` or `EnumSetting(name, default, entries)` with an enum implementing `StoredEnum` (its `stored` string is what is saved; never change it).
2. Add rows in your own `ui/settings/<Feature>Section.kt` with `SettingsGroup`, `SwitchSetting`, `ChoiceSetting`.
3. Register the section with one line in `ui/settings/SettingsSections.kt`. Read the value elsewhere with `SettingsStore[setting]` (a `Flow`).

```kotlin
object GestureSettings { val SwipeDownNotifications = BooleanSetting("swipe_down_notifications", default = true) }
@Composable fun GestureSection(settings: SettingsViewModel) = SettingsGroup(R.string.settings_gestures) {
    SwitchSetting(settings, GestureSettings.SwipeDownNotifications, title = R.string.settings_swipe_down)
}
```

## 5. Quick Commands

```bash
./gradlew :app:assembleDebug          # Build debug APK (installs as com.wander.mono.debug)
./gradlew :app:testDebugUnitTest      # Run local unit tests
./gradlew :app:lintDebug              # Run lint checks
./gradlew :app:detekt                 # Static analysis
```

The Android SDK lives on the Windows side: build from Android Studio or a Windows shell (`gradlew.bat`).
