<p align="center">
  <img src="docs/assets/mono-logo.png" width="96" height="96" alt="Mono logo" />
</p>

<h1 align="center">Mono</h1>

<p align="center">
  A quiet, minimalist, battery-first Android launcher built under EUPL-1.2.
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-EUPL--1.2-blue" alt="License"/></a>
  <img src="https://img.shields.io/badge/made%20in-%F0%9F%87%AB%F0%9F%87%B7%20France-002395" alt="Made in France"/>
  <img src="https://img.shields.io/badge/network-none-5b1027" alt="No network permission"/>
  <img src="https://img.shields.io/badge/built%20for-GrapheneOS-f3b9c6" alt="Built for GrapheneOS"/>
</p>

<p align="center">
  <a href="https://github.com/Kolbxyz/Mono/releases">Releases</a> ·
  <a href="https://github.com/Kolbxyz/Phalanx">Phalanx</a> ·
  <a href="docs/design/ui-ux-spec.md">Design spec</a> ·
  <a href="CLAUDE.md">Engineering rules</a>
</p>

---

Mono is a home screen with almost nothing on it: your favourite apps as **one vertical list of names**, and what they are doing right now shown **inline, right under the app doing it**. The song that is playing, your next meeting, the last message, a widget: each is a row next to its app, never a screen of its own.

Swipe up for every app, alphabetical, with a letter rail and a search that also finds shortcuts, contacts, settings pages and sums.

Built for hardened phones like GrapheneOS, in strict **Material 3 Expressive**: no network permission, no telemetry, no Google services.

---

## Welcome to Mono! (FAQ for Newcomers)

<details>
<summary><b>Why "Mono", and what is the 物 in the logo?</b></summary>
<br>
物 (<i>mono</i>, "thing") is the first word of <i>mono no aware</i> (物の哀れ): the quiet feeling for things that pass. Mono keeps only a few things in front of you, and lets the rest go.
</details>

<details>
<summary><b>Why a list of names instead of a grid of icons?</b></summary>
<br>
A grid asks you to look; a list lets you read. Mono keeps a short list of the apps you chose, in your order, so opening one is a glance and a tap. Icons can stay in full colour, follow your wallpaper's theme, or disappear entirely.
</details>

<details>
<summary><b>Does Mono need the internet?</b></summary>
<br>
No. Mono does not even request the <code>INTERNET</code> permission, so Android itself prevents it from connecting anywhere. A web search from the drawer is handed to your browser; Mono never sees the result.
</details>

<details>
<summary><b>Why does it ask for notification access?</b></summary>
<br>
Now playing, notification dots and message previews all come from the notification listener. Mono asks once, with a single row on home, and works without it. On Android 13+ a sideloaded app must first be allowed "restricted settings" from its App info menu.
</details>

<details>
<summary><b>Do work profiles work?</b></summary>
<br>
Yes. Every app, shortcut and launch goes through Android's <code>LauncherApps</code>, so work profile apps appear badged and behave like the system expects.
</details>

<br>

---

## Key Capabilities

### Home
* **Glance header**: A big clock, the date, your next alarm and, when no calendar row is shown, your next event.
* **Favourites in your order**: Hold to lift and drag; rename an app's label, remove or hide it from the same sheet.
* **Live rows**: Now playing (tinted by its artwork, with up next, seek by drag and the output device), notification previews with inline reply, upcoming events and hosted widgets, each under its app and switchable per app.
* **Quiet mode**: Home shows only the apps you keep for quiet hours, with an optional Do Not Disturb of Mono's own and a Quick Settings tile.

### Drawer & Search
* **Alphabetical list with a letter rail**: A wave rail with a letter bubble and haptic ticks, adjustable with TalkBack.
* **Ranked search**: Ignores case and accents and matches initials ("ym" finds YouTube Music); the keyboard's Go key opens the top result.
* **More than apps**: App shortcuts (such as Phalanx conversations), a calculator, settings pages, opt-in contacts and a hand-off to your browser.

### Your apps, your way
* **Icon modes**: Full colour, themed monochrome or text only.
* **Hidden apps**: Out of the drawer and search, optionally behind your screen lock.
* **Today's usage**: Long-press an app to see how long you spent in it today.
* **Sibling apps**: With [Wanda](https://github.com/AgroUPlus/Wanda) or [Phalanx](https://github.com/Kolbxyz/Phalanx) installed, Mono shows their up next, searches your music library and counts unread per room; without them, nothing appears.
* **Themes**: System, light or dark, AMOLED black, colours from your wallpaper.

---

## Battery & Privacy Discipline

### Battery First
* **Zero Polling**: Mono reacts to `LauncherApps` callbacks, system broadcasts and listener events; only what has no event is re-checked when home comes back.
* **Event-Bound Calendar**: Upcoming events are observed and re-read only when one starts or ends.
* **No Background Work of Its Own**: No alarms, no workers, no foreground service; only the notification listener and the quiet mode Quick Settings tile, both run by the system.

### Hardened Privacy
1. **No Network, Ever**: No `INTERNET` permission. Nothing leaves the device.
2. **Zero Telemetry**: No Firebase, no Google Play Services, no crash reporting, no analytics of any kind.
3. **Opt-In Everything**: Notifications, calendar, contacts, usage and Do Not Disturb are each asked for only when you turn on the feature that needs them.
4. **Never Cached**: Contacts are looked up as you type and never stored.
5. **Locked Hidden Apps**: Revealing hidden apps can require your screen lock.

---

## Architectural Principles

The codebase enforces strict engineering rules documented in [CLAUDE.md](CLAUDE.md) and [`docs/design/`](docs/design/ui-ux-spec.md):

* **300 Lines Max Per File**: Split at 250 lines; one concept per file.
* **`LauncherApps` Is the App Source**: Apps, profiles, shortcuts, launch and app info, so work profiles behave.
* **Rows, Not Screens**: Home is a list of sealed `HomeRow`s; live content sits next to its app.
* **Strict Material 3 Expressive**: `MaterialExpressiveTheme`, wallpaper dynamic colour, motion from the theme's motion scheme, explicit edge-to-edge insets.

```
app/src/main/java/com/wander/mono/
├── core/
│   ├── apps/           LauncherApps catalog, shortcuts, icon cache, launch and uninstall
│   ├── calendar/       Opt-in upcoming events, re-read at event boundaries
│   ├── contacts/       Opt-in contact lookup for search, never cached
│   ├── media/          Active media sessions, queue, progress and outputs
│   ├── notifications/  Notification listener, counts and previews
│   ├── quiet/          Quiet mode, its Do Not Disturb rule and QS tile
│   ├── security/       Screen lock for hidden apps
│   ├── siblings/       Wanda and Phalanx providers: up next, library search, unread
│   ├── system/         Clock, alarm, wallpaper, settings pages, web hand-off
│   ├── usage/          Today's foreground time per app
│   └── widgets/        App widget host, providers and placements
├── data/               DataStore: favourites, hidden apps, labels and settings
├── di/                 Hilt modules and the app scope
└── ui/                 Home, drawer, sheets, edit mode, settings, onboarding, theme
```

---

## Installation & Updates

* **Obtainium**: Add `Kolbxyz/Mono` to [Obtainium](https://github.com/ImranR98/Obtainium) for updates straight from GitHub releases.
* **GitHub Releases**: Download signed APKs from the [Releases](https://github.com/Kolbxyz/Mono/releases) tab.

After installing, set Mono as your home app; first-run onboarding walks you through it and notification access.

---

## Building from Source

Requires **JDK 25** (the build targets Java 17) and **Android SDK 37** (compileSdk 37, minSdk 26).

```bash
# Build debug APK (installs as com.wander.mono.debug)
./gradlew :app:assembleDebug

# Run unit tests, lint and static analysis
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:detekt
```

---

## License

**EUPL-1.2 (European Union Public Licence v1.2)**: see [`LICENSE`](LICENSE).
