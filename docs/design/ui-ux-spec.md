# Mono — UI/UX spec

Status: v1, aligned with the shipped app (2026-10-30). Scope: home, drawer/search, inline widget rows, settings, onboarding.
Reference screenshots live in `docs/design/references/<launcher>/` (local only, gitignored; sources in `references/SOURCES.md`). Cited below as `ref:<launcher>/<NN>`.

Hard constraints from `AGENTS.md` that shape every decision here: no `INTERNET` permission, no polling, `LauncherApps` as the app source, rows not screens (`HomeRow`), strict Material 3 Expressive with `MaterialTheme.motionScheme`, no Google services, strings in `strings.xml`, never placeholder data.

---

## 1. Design principles

1. **One calm column.** Everything important lives in one vertical list. No pages, no grid, no dock. If it is not worth a row, it is not on home.
2. **Live where it belongs.** Live content sits directly under the app it comes from (now-playing under Wanda, unread under Phalanx). Context is never moved to a separate dashboard.
3. **Quiet by default, rich on request.** At rest a row is one line of big text. Detail (artwork, previews, controls) appears only when there is something live, and expands only on touch.
4. **Legible on any wallpaper.** Typography and scrims adapt to the wallpaper; we never trust the user's wallpaper to be "nice".
5. **Private and honest.** All data stays on device; every permission is asked for in context with the exact reason; missing permissions show a real prompt row, never fake data.

---

## 2. What we take / reject from each reference

| Launcher | Take | Reject | Refs |
| --- | --- | --- | --- |
| **Niagara** | Single left-aligned list with large labels; glance header (clock + date) directly above the list; right-edge alphabet rail with a *wave* that bulges toward the finger; music card inline in the list; notification preview opening from the row with reply + smart chips; text-first look on dark wallpapers | Decorative display clocks ("MON" split typography) as default; agenda+weather as a separate bottom sheet (we inline it); theme marketplace | `ref:niagara/01`, `03`, `07`, `09`, `06`, `11` |
| **Kvaesitso** | Widgets as a vertical column of full-width cards (weather/music/calendar), "Edit widgets" mode; search that returns typed result cards (calculator, unit converter, files) | Cards stacked as a separate screen away from apps; heavy opaque cards on wallpaper; Wikipedia/web results (needs network) | `ref:kvaesitso/02`, `04`, `05`, `06` |
| **Smart Launcher 6** | Glance header that carries *next event + alarm* in two quiet lines; responsive widgets that reflow to the width they get; locked private space concept | Glassmorphism grid, icon bubbles, colour pickers with hex values (breaks dynamic colour), Google search pill | `ref:smart-launcher/01`, `03`, `06`, `02`, `04` |
| **Lawnchair** | Themed monochrome icons that follow dynamic colour; At a Glance toggles per data source (date, battery, now playing) in settings; plain M3 settings list | Grid home, dock, Google search bar | `ref:lawnchair/02`, `04`, `03` |
| **Olauncher** | Text-only mode: no icons, generous line height, nothing else; keyboard-first drawer (type to filter) | Hard-coded white text with no scrim (fails on light wallpapers, see snow in `02`) | `ref:olauncher/01`, `02`, `03` |
| **Before Launcher** | Notification *filter*: chosen apps go to a quiet inbox instead of buzzing; drawer sections (pinned / recent / A–Z) | Right-aligned list (hurts scanning for LTR); filtered inbox as a separate screen | `ref:before-launcher/02`, `03`, `01` |
| **Ratio** | Usage-awareness on the long-press sheet; "essentials vs favourites" tiers | Neon accent tiles, dense grid, paid/proprietary model | `ref:ratio/01`, `03` |
| **Nothing Launcher** | Bold, single-purpose widgets that read in one glance; big shape-masked icons as a deliberate *focus* element | Dot-matrix brand typography and red accent (trade dress); icon grid | `ref:nothing-launcher/01`, `03` |
| **Android 16 system UI / M3 Expressive** | Media card with wavy progress (`LinearWavyProgressIndicator`) and pill play button; expressive shape morphing on press; emphasized type; large rounded tonal containers | Copying Pixel's clock font or At a Glance layout literally | `ref:android16-system-ui/02`, `03`, `ref:m3-expressive/02`, `03` |

---

## 3. Information architecture

```
Home (list)  ── swipe up / tap search ──▶  Drawer + Search (one surface)
   │                                              │
   ├─ long-press row ─▶ Row sheet                 ├─ long-press app ─▶ App sheet
   ├─ long-press empty / pinch ─▶ Edit mode       └─ ⋮ ─▶ Settings
   └─ swipe down ─▶ system notification shade
```

| Place | Lives here | Why |
| --- | --- | --- |
| **Home** | Glance header, favourites (≤ ~9), widget rows attached to a favourite or free-standing (calendar), permission prompt rows | One-thumb daily surface; everything here earns its pixels. |
| **Drawer/search** | All apps A–Z, letter rail, search (apps, shortcuts incl. Phalanx conversations, settings, calculator, contacts later), hidden apps entry at the very end | Search and browse are the same task: find the thing. Keyboard opens immediately when entered via the search gesture. |
| **Row sheet** (bottom sheet) | Per-row actions: reorder, remove, attach/detach widget row, app shortcuts, hide, app info, uninstall | Keeps home free of chrome. |
| **Edit mode** | Drag to reorder, add widget row, add favourite, row density | Spatial edits need a spatial mode, not a settings page. |
| **Settings** | Appearance, home, widgets & live rows, drawer & search, gestures, privacy & permissions, quiet mode, about | Configuration that is rarely touched. Reached from drawer ⋮ and from edit mode. |

---

## 4. Home layout

### 4.1 Anatomy (default, with icons)

```
┌──────────────────────────────────────┐
│ status bar (transparent)             │
│                                      │
│  09:41                               │  displayLarge, emphasized
│  Thu 29 Oct · ⏰ 07:00 · Standup 10:00│  titleMedium, onSurfaceVariant
│                                      │
│                                      │  ← flexible space (list bottom-anchored)
│  ◐  Wanda                            │  headlineSmall label
│  ┌──────────────────────────────────┐│
│  │ ▣  Harvest Moon                  ││  now-playing row (inline card)
│  │    Neil Young     ⏮  ( ▶ )  ⏭   ││
│  │ ∿∿∿∿∿∿∿∿∿∿──────────────          ││  wavy progress
│  └──────────────────────────────────┘│
│  ◐  Phalanx                     ● 3  │  badge count
│     Léa: on se voit à 19h ?          │  notification preview line (1)
│  ◐  Calendar                         │
│     10:00 Standup · 14:30 Dentist    │  calendar row (compact)
│  ◐  Camera                           │
│  ◐  Firefox                          │
│  ◐  Signal                      ● 1  │
│                                      │
│ ( ⌕  Search apps, settings…     ⠿ ) │  search dock (thumb zone)
└──────────────────────────────────────┘   letter rail hidden on home
```

- **Bottom-anchored list.** Favourites stack upward from the search affordance, so the most-used rows are in the thumb zone (Niagara and Olauncher both centre/bottom their lists: `ref:niagara/09`, `ref:olauncher/01`). Header stays top-left.
- **Row order is user order.** Live rows never reorder favourites; they only expand under their owner.
- **Search dock** is a 56 dp `surfaceContainerHigh` pill with the search hint, opening the drawer; plus the swipe-up gesture. Opaque and tonal, no logo or brand (what `ref:lawnchair/01` got wrong was the Google bar, not the pill).

### 4.2 Glance header

| Slot | Source (no network) | Shown when |
| --- | --- | --- |
| Time | `core/system` clock tick (`ACTION_TIME_TICK`) | Always |
| Date | Same | Always |
| Next alarm | `AlarmManager.getNextAlarmClock()` + `ACTION_NEXT_ALARM_CLOCK_CHANGED` | Alarm within 24 h |
| Next event | `CalendarContract` (needs `READ_CALENDAR`), observed via `ContentObserver` | Permission granted, event within 12 h, and no calendar row on home |
| Battery | `ACTION_BATTERY_CHANGED` sticky | Only when ≤ 15 % or charging (quiet by default) |

- Tap time → clock app; tap date/event → calendar app at that day; tap alarm → alarm list.
- No weather: requires network. Do not offer it (no fake fallback).
- Header text is a single `Column`; each slot is a `Text` with its own `contentDescription`-friendly string.

### 4.3 Favourite row

| Property | Value |
| --- | --- |
| Height | 56 dp min (default density), 48 dp (compact), 64 dp (comfortable) |
| Label | `headlineSmall` (24 sp) default; `titleLarge` compact; `headlineMedium` comfortable |
| Icon | 32 dp, themed monochrome if available (`AdaptiveIconDrawable.getMonochrome()`), else full icon; or none in text-only mode |
| Badge | Count pill (`Badge`) aligned to label baseline, right edge 24 dp; dot only if count unknown |
| Work profile | Small briefcase badge on icon (from `LauncherApps` user badge) |
| Press | Shape morph of the row's pressed container (rounded 12 → 28 dp, `MaterialShapes` not needed) + label scale 0.98 |

### 4.4 Inline widget rows

All live rows are `HomeRow` subtypes, drawn by Mono in Compose (not hosted `AppWidget`s) unless stated. They hang off an *owner* favourite and render directly under it, inset 16 dp from the label start, inside a `surfaceContainer` card with `shapes.large` (16 dp) corners.

| Row | Owner | Data source | Collapsed | Expanded (tap) |
| --- | --- | --- | --- | --- |
| **Now playing** | App owning the active `MediaSession` (Wanda, or any player) | `MediaSessionManager.getActiveSessions` via listener grant; callbacks only | Artwork 48 dp, title/artist, play-pause pill, wavy progress | Artwork 96 dp, prev/next, seek by drag on wavy bar, output device chip (`ref:android16-system-ui/02`) |
| **Unread (Phalanx and any messenger)** | App with notifications | `MonoNotificationListener` | Count badge + latest sender: one-line preview | Up to 3 conversations (`MessagingStyle`), inline reply field, actions (Mark read, Mute) (`ref:niagara/07`) |
| **Calendar** | Calendar app or free-standing | `CalendarContract.Instances`, `ContentObserver` | Next 2 events today, one line | Today + tomorrow agenda, tap event → calendar |
| **Notification preview** (generic) | Any favourite with a non-silent notification | Listener | One-line title · text, 2 lines max | Full text + notification actions as tonal chips |
| **Hosted widget** (later) | Any app | `AppWidgetHost` with size from row width | Fixed height per widget, M3 card wrapper | n/a (the widget handles its own taps) |

Rules:
- A live row appears only with real data. No session → no now-playing row. No permission → a single prompt row (existing pattern).
- Max **one expanded row** at a time; expanding another collapses the previous.
- Live rows can be **disabled per app** (row sheet → "Show live row").
- Sibling-app contract (Wanda, Phalanx): optional signature-protected `ContentProvider`/broadcast so they can publish richer, non-notification data (Wanda: "Up next"; Phalanx: unread per room even when notifications are muted). Same signing key, `protectionLevel="signature"`, no network. Mono works fully without it.

### 4.5 Empty state (first launch, no favourites)

```
│  09:41                               │
│  Thu 29 Oct                          │
│                                      │
│  Your calm place.                    │  headlineMedium
│  Pick the few apps you use every day.│  bodyLarge, onSurfaceVariant
│                                      │
│  [ Choose favourites ]               │  filled button (M3 Expressive, round)
│  Swipe up for all apps               │  labelLarge hint
```

### 4.6 Edit mode

Entry: long-press empty space, or row sheet → "Edit home".

```
│  Edit home                    [Done] │  top bar, Done = filled tonal
│  ≡  ◐ Wanda                    ⊖     │  drag handle ≡, remove ⊖
│     └ ▣ Now playing        [On ●]    │  live row toggle, indented
│  ≡  ◐ Phalanx                  ⊖     │
│     └ ✉ Unread preview     [On ●]    │
│  ≡  ▦ Calendar (free row)      ⊖     │
│  ≡  ◐ Camera                   ⊖     │
│                                      │
│  [ + App ]  [ + Live row ]  [ Density ▾ ]  │  floating toolbar (M3E)
```

- Wallpaper dims (scrim to 60 %) to signal the mode; list rows get `surfaceContainerHigh` backgrounds.
- Drag uses haptic `SEGMENT_TICK` on each slot change, `GESTURE_END` on drop.
- Accessible alternative: each row exposes custom actions "Move up", "Move down", "Remove".

---

## 5. Visual system

### 5.1 Type scale

Font: **Google Sans Flex** bundled as a variable font (`res/font/google_sans_flex.ttf`, OFL, the same file Wanda and Phalanx ship; no GMS downloadable fonts). Display, headline and title styles set the `ROND` axis to 65 and run heavier (SemiBold to ExtraBold, tight tracking); body and label styles stay at `ROND` 0. Scale in `ui/theme/Type.kt`.

| Role | M3 token | Size / line | Use |
| --- | --- | --- | --- |
| Clock | `clockStyle` | 88/92 sp, ExtraBold, -2 sp, tabular figures; AM/PM at 0.4 em | Header time |
| Header meta | `titleMedium` | 16/24 sp | Date, alarm, event |
| Row label (default) | `headlineSmall` | 24/32 sp | Favourites |
| Row label (compact) | `titleLarge` | 22/28 sp | Dense lists |
| Live row title | `titleMedium` | 16/24 sp | Track, sender |
| Live row body | `bodyMedium` | 14/20 sp | Artist, preview |
| Drawer item | `titleLarge` | 22/28 sp | All apps |
| Rail letter | `labelMedium` | 12/16 sp | Alphabet |
| Rail bubble | `displaySmall` | 36/44 sp | Scrub indicator |

Clock styles (setting): Default (above), Stacked (hours over minutes, like `ref:kvaesitso/01` / `ref:android16-system-ui/01`), Small (one line). No novelty fonts.

### 5.2 Spacing grid

- Base unit **4 dp**; layout steps 8 / 12 / 16 / 24 / 32.
- Screen side gutter **24 dp** (compact width), **32 dp** (medium); rows never span tablets wider than 560 dp — centre the column (`ref:niagara/09` foldable).
- Icon ↔ label gap 16 dp. Row vertical padding 8 dp. Live card inner padding 16 dp, outer inset start = label start.
- Header → list gap ≥ 32 dp; list → search affordance 16 dp.

### 5.3 Shapes (M3 Expressive)

| Element | Shape |
| --- | --- |
| Scale | 8 / 12 / 18 / 26 / 36 dp (`ui/theme/Shape.kt`), rounder than baseline, shared with Wanda and Phalanx |
| Grouped lists (drawer letters, search results, settings, sheets) | Segmented: 26 dp outer corners, 8 dp at the seams, 2 dp gaps (`SegmentedColumn`) |
| Live row card | `shapes.large` (26 dp) → `extraLarge` (36 dp) when expanded (morph) |
| Play/pause | Round → squircle on press (`ToggleButton`/`IconButton` shape morph) |
| Rail bubble | `MaterialShapes.Cookie12Sided`, scaling up from 60 % on scrub start and turning with the finger |
| Sheets | `extraLarge` top corners on `surfaceContainerLow`; main actions as tall tonal tiles whose corners tighten on press |
| Edit toolbar | `FloatingToolbar` (fully rounded) |
| Icon mask (full-colour adaptive icons) | System mask by default; setting: Circle, Squircle, Cookie (`Cookie9Sided`), Clover (`Clover4Leaf`). Work icons keep the system mask under their badge |

### 5.4 Colour over arbitrary wallpapers

- Theme: `MaterialExpressiveTheme` with `dynamicDarkColorScheme`/`dynamicLightColorScheme` from wallpaper.
- **Legibility scrim:** home draws a vertical gradient scrim behind header and list only (not the whole screen): `surface` at alpha α at the top and bottom edges, held over the upper header, then eased (smoothstep, no visible band) to 0 over 120 dp past the header and before the list. A soft `surface` halo under home text carries legibility where the scrim thins. α is computed once per wallpaper change from `WallpaperManager.getWallpaperColors()` + `HINT_SUPPORTS_DARK_TEXT`:
  - dark-text-friendly wallpaper in light theme → α 0.35, `onSurface` text;
  - otherwise dark scheme text, α 0.45 (0.65 if wallpaper is busy, see below).
  - Listen to `WallpaperManager.OnColorsChangedListener` (no polling).
- **Busy wallpaper fallback:** optional "Text shadow" setting (1 dp y-offset, 4 dp blur, `scrim` 40 %) for users who refuse a scrim (fixes the `ref:olauncher/02` snow case).
- Target contrast: row labels ≥ 4.5:1 against the scrimmed wallpaper; badges and icons ≥ 3:1.
- Live cards are opaque `surfaceContainer` (never glass), so their contents always meet contrast regardless of wallpaper (reject `ref:smart-launcher/02`).
- Now-playing card may tint from artwork (`Palette`-free: derive seed with `dynamicColorScheme`-style quantization in-app, or keep `secondaryContainer`) — must still pass 4.5:1.

### 5.5 Icons vs text-only

| Mode | Look |
| --- | --- |
| **Icons (default)** | 32 dp themed monochrome icons in `onSurfaceVariant`; full-colour fallback for apps without monochrome layer |
| **Colour icons** | Original icons, masked |
| **Text only** | No icons; label start = gutter; badges remain (`ref:olauncher/01`) |

### 5.6 Themes

| Theme | Behaviour |
| --- | --- |
| System | Follows dark/light |
| Light / Dark | Forced |
| **AMOLED** | Dark scheme with `surface*` roles mapped to `#000000` and container roles lifted by 4–8 % lightness; with it on, a "Solid black wallpaper" toggle makes the window opaque black instead of showing the wallpaper |

---

## 6. Motion & haptics

All specs from `MaterialTheme.motionScheme` (Expressive scheme via `MaterialExpressiveTheme(motionScheme = MotionScheme.expressive())`). Reduced motion → `NoMotionScheme` (existing).

| Transition | Spec | Notes |
| --- | --- | --- |
| Drawer open/close (swipe-up) | `defaultSpatialSpec()` for offset, gesture-driven with `AnchoredDraggable`; `defaultEffectsSpec()` for scrim alpha | Follows finger 1:1; settles with spring overshoot on release |
| Drawer → search keyboard | `fastEffectsSpec()` fade of list chrome | IME inset animated by `WindowInsets.ime` (system) |
| Row expand (live row) | `defaultSpatialSpec()` for height (`animateContentSize`), shape morph large→extraLarge via same spec; content `fastEffectsSpec()` fade-in after 50 % | One expanded at a time |
| Widget row appear (media starts) | `slowSpatialSpec()` height from 0 + `defaultEffectsSpec()` alpha | Row below slides down; never jumps |
| Widget row disappear | `fastSpatialSpec()` | Leave quickly |
| Badge count change | `fastSpatialSpec()` scale 0.8→1 | No bounce for decrements |
| Letter rail scrub | Wave offset per letter driven by finger y, `fastSpatialSpec()` to follow; bubble scale `defaultSpatialSpec()` and rotation following the finger | Wave amplitude 72 dp, raised-cosine falloff over ±4 letters (`ref:niagara/03`) |
| Press feedback | `fastSpatialSpec()` shape/scale | |
| Edit mode enter | `defaultEffectsSpec()` scrim + `defaultSpatialSpec()` handles slide in | |
| Sheets | M3 `ModalBottomSheet` defaults | |

Haptics (`View.performHapticFeedback`, respects system setting):

| Event | Constant |
| --- | --- |
| Rail letter change | `SEGMENT_FREQUENT_TICK` (fallback `CLOCK_TICK`) |
| Long-press opens sheet | `LONG_PRESS` |
| Drag slot change (edit) | `SEGMENT_TICK` |
| Drop / drawer settle open | `GESTURE_END` |
| Play/pause, send reply | `CONFIRM`; failure (`ActionResult.Failed`) → `REJECT` |

---

## 7. UX flows

### 7.1 First run / onboarding

Shipped (`ui/onboarding`). Each step is a full-screen pane with one primary action and a "Skip" text button. Never blocks: Mono works with zero permissions. Steps the system already reports done are passed over, also on the way back from a system screen.

1. **Welcome** — "A calm home for your phone." Primary: *Continue*.
2. **Set as home** — `RoleManager.createRequestRoleIntent(ROLE_HOME)`. If already holder, auto-skip. If declined, show how to change later (Settings → Apps → Default apps).
3. **Pick favourites** — checklist of launchable apps (`LauncherApps`, no private space). With usage access, ordered by today's foreground time and the top 5 used for at least a minute pre-ticked; otherwise A–Z with nothing ticked. Past 9 ticks: "Fewer is calmer". Order = tick order; *Add to home* writes them, Skip writes nothing.
4. **Live rows (optional)** — Explains now-playing, unread previews, badges. Primary: *Allow notification access*.
   - 4a. **Restricted settings check (Android 13+ / GrapheneOS):** if access is still off after asking, a 3-step card: ① *Open App info* ② tap ⋮ → *Allow restricted settings* and confirm ③ come back and *Try again*. Copy explains this is an Android safety step, not a Mono requirement.
   - 4b. Open `ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS` with Mono's component.
5. **Calendar (optional)** — `READ_CALENDAR` request with rationale "Nothing leaves your phone"; granted turns the Calendar setting on, Skip leaves it off.
6. **Done** — lands on home with a one-time "Swipe up for all apps" chip, shown until the drawer is first opened.

Until it is known whether setup was done (a few ms on a cold start) nothing is drawn over home, so normal starts never flash a surface; setup then appears without a fade.

### 7.2 Adding a favourite

1. Drawer → long-press app → sheet → *Add to home*. (Or edit mode → *+ App* → picker.)
2. Row is inserted at the bottom of the list (closest to thumb); toast-free: the drawer closes and the new row pulses once (`defaultEffectsSpec` highlight).
3. If the app has an active session/notification, its live row appears immediately.

### 7.3 Adding / positioning a widget row

1. Edit mode → *+ Live row*.
2. Sheet lists available row types with live preview built from **real** data only (Now playing, Notification preview, Calendar, later: hosted widgets). Types without permission show "Needs access" and route to the grant flow.
3. Attached types (now playing, previews) ask for the owner app (default: the obvious one); free-standing types (calendar) are inserted below the selected position.
4. Drag handle positions free-standing rows; attached rows move with their owner.
5. *Done* → rows animate to rest.

### 7.4 Searching & launching

1. Swipe up anywhere on home (or tap ⌕) → drawer; tapping ⌕ or swiping up a second time from drawer top focuses search and opens IME.
2. Typing filters instantly (case/accent-insensitive, initials match — existing ranking). Results order: calculator result → apps (best first) → shortcuts (e.g. Phalanx rooms) → contacts (opt-in) → settings → "Search in <browser>" (only when nothing else matched; the browser does the searching).
3. *Go* on keyboard launches the top result; the top result has a tonal highlight so users know what Go does.
4. Launch: app opens with the system launch animation from the row's bounds (`ActivityOptions.makeClipRevealAnimation` / `makeScaleUpAnimation`).
5. Back/home returns to a cleared search.

### 7.5 Scrubbing the alphabet rail (drawer)

1. Rail sits right edge, 24 dp wide touch target 48 dp, letters only for those present; ★ at top for favourites (`ref:niagara/01`).
2. Touch-down: bubble appears left of finger with current letter; letters around the finger bow out in a wave.
3. Move: list jumps to section start (`scrollToItem`, not animated, during scrub); haptic tick per letter.
4. Release: bubble morphs back and fades; list stays. Lift outside rail cancels nothing — position is kept.
5. TalkBack: rail is replaced by a "Jump to letter" action on the list and an adjustable rail node (swipe up/down = next/prev letter).

### 7.6 Interacting with now-playing

| Gesture | Action |
| --- | --- |
| Tap play/pause | `transportControls.play()/pause()` |
| Tap card body | Expand (prev/next, seek, output chip) |
| Tap artwork / title | Open owner app (`sessionActivity` PendingIntent) |
| Horizontal swipe on card | Next / previous track |
| Drag on wavy bar (expanded) | Seek (`seekTo`), if `ACTION_SEEK_TO` supported |
| Long-press card | Row sheet: hide live row for this app |
| Session ends | Row collapses after 10 min paused, or immediately when the session is destroyed |

### 7.7 Reading / replying to a notification inline

1. Phalanx row shows "● 3" and one preview line (latest message, `MessagingStyle` sender: text).
2. Tap preview → row expands: up to 3 conversations, each with last 2 messages.
3. Tap *Reply* chip → inline `TextField` appears in the row, IME opens, list scrolls to keep field above IME (`imePadding`).
4. Send → fires the notification's `RemoteInput` action; on success field clears and haptic `CONFIRM`; on failure an inline error text (no silent drop).
5. *Mark read* / other actions → fire the notification's own actions; Mono never invents actions.
6. Tap conversation title → opens the notification's `contentIntent` (deep link into the room).
7. Sensitive content: if the system redacts content (Android 15 OTP redaction, lock-screen privacy), show "Content hidden", not a guess.

### 7.8 Hiding apps

1. Drawer long-press → *Hide*. Snackbar with *Undo*.
2. Hidden apps disappear from drawer and search.
3. Drawer bottom: "Hidden apps (n)" row → list with *Unhide*. Optional setting: require device credential (`BiometricPrompt` with `DEVICE_CREDENTIAL`) to view hidden apps.
4. For real separation, point users to Android **Private space** / work profile; Mono shows those profile apps badged (inspired by `ref:smart-launcher/06`, but using the OS feature).

### 7.9 Focus / quiet mode

1. Toggle from: long-press clock → *Quiet mode*, or Settings, or a Quick Settings tile (`TileService`).
2. Effects (shipped): home shows only the favourites chosen as quiet apps, without badges or previews; every other row is hidden rather than dimmed. Opening the drawer meets a gate ("Quiet mode is on") with *Show all apps* and *Back to home*, the friction Before-style launchers add (`ref:before-launcher/01`).
3. Optional: link to an Android **Mode** via `NotificationManager.addAutomaticZenRule` (needs `ACCESS_NOTIFICATION_POLICY`), so DND and Mono's quiet mode switch together.
4. Header shows a small "Quiet" chip; tap → end now / for 1 h.

---

## 8. Gesture map

| Where | Gesture | Default action | Configurable |
| --- | --- | --- | --- |
| Home | Swipe up | Open drawer | Yes (drawer / search with keyboard) |
| Home | Swipe down | Expand notification shade | Yes (shade / quick settings / search) |
| Home | Double-tap empty | Nothing by default; optional "Lock screen" needs an opt-in accessibility service (`GLOBAL_ACTION_LOCK_SCREEN`), explained before enabling | Yes, off by default |
| Home | Long-press empty | Edit mode | No |
| Home | Long-press clock | Quiet mode / clock settings sheet | No |
| Row | Tap | Launch app | No |
| Row | Long-press | Row sheet | No |
| Row | Swipe right on row with notifications | Expand preview | No |
| Live card | Tap / swipe ← → / long-press | See 7.6 | No |
| Drawer | Scroll down at top / back | Close drawer | No |
| Drawer | Touch rail | Scrub letters | No |
| Drawer | Long-press app | App sheet | No |
| Search | Keyboard Go | Launch top result | No |
| Any | System back | Collapse expanded row → close drawer → nothing on home | No |

---

## 9. Accessibility

| Area | Requirement |
| --- | --- |
| **TalkBack** | Each row is one focusable node: "Phalanx, 3 unread. Double-tap to open." Live card is a separate node with custom actions (Play/Pause, Next, Previous, Expand, Reply). Rail: adjustable node + list "Jump to letter" action. Edit mode: Move up/down/Remove custom actions. Header slots are individual nodes. |
| **Font scale 200 %** | Rows grow in height (no fixed heights; `heightIn(min=)`); labels ellipsize after 1 line, never clip. Clock caps at 1.3× scale to keep list visible. Live cards switch to stacked layout (controls below text) above 1.5×. Verify with `@Preview(fontScale = 2f)`. |
| **Reduced motion** | `NoMotionScheme`; wave rail becomes a flat highlight; no shape morphs; drawer still follows the finger (direct manipulation is not "motion"). Respect `Settings.Global.ANIMATOR_DURATION_SCALE == 0`. |
| **Contrast on wallpaper** | Scrim algorithm (5.4) targets 4.5:1 for labels; "Increase contrast" setting raises α by 0.2 and switches icons to full-contrast `onSurface`. Honour system contrast level (`UiModeManager.getContrast()`, Android 14+). |
| **One-handed reach** | Bottom-anchored list; search affordance and edit toolbar in bottom 40 %; rail usable with thumb (right side, mirrored in RTL); option to pin rail to left. |
| **Touch targets** | ≥ 48 dp for every interactive element, including rail letters (the visual letter is smaller). |
| **RTL** | Mirror rail, gutters and swipe-to-expand direction. |
| **Colour-independence** | Badges carry numbers; quiet-mode dimming also adds a "Paused" label for TalkBack. |

---

## 10. Settings structure

A hub of categories (`ui/settings/SettingsCategory.kt`), each a page of sections (`SettingsSections.kt`):

| Category | Contents |
| --- | --- |
| Home & clock | Clock preview and face, glance lines, home look (text shadow), headphone apps, calendar |
| Drawer & search | Drawer background, search sources (contacts) |
| Appearance & icons | Theme (System/Light/Dark, AMOLED black, solid black wallpaper), icons (colour/themed/text only, shape, pack), accessibility (high contrast) |
| Notifications | Badges, previews, quiet mode (linked Do Not Disturb mode, quiet apps) |
| Gestures | Swipe down, double tap to lock (accessibility service) |
| Privacy & about | "What Mono can see" (no internet permission; each access with its live state and a link to Android's page), hidden apps lock, usage access, backup, version and font licences |

Plain M3 list settings (`ref:lawnchair/03`): `ListItem` with leading icon, headline + supporting text; toggles as trailing `Switch`.

---

## 11. Roadmap

Effort: **S** ≤ 1 day, **M** 2–4 days, **L** ≥ 1 week. Status (2026-10-30): every item below has shipped; where the code deliberately differs from earlier sections, those sections now describe the code.

### v0.2 — "Calm list, done right"
| Item | Effort |
| --- | --- |
| Bottom-anchored favourites + new type scale & spacing grid | S |
| Wallpaper-aware legibility scrim (`OnColorsChangedListener`) + text-shadow option | M |
| Themed monochrome icons + text-only mode | S |
| Now-playing row v2: wavy progress, expand to controls, swipe next/prev | M |
| Badge counts (not just dots) | S |
| Wave alphabet rail with bubble, haptic ticks, TalkBack adjustable | M |
| Onboarding flow incl. restricted-settings helper, favourites and calendar steps | M |
| Empty state | S |
| AMOLED theme + solid black wallpaper | S |

### v0.3 — "Hub"
| Item | Effort |
| --- | --- |
| Notification preview row + inline reply (RemoteInput) | L |
| Calendar row + header next event (`CalendarContract`) | M |
| Edit mode (drag reorder, live-row toggles, floating toolbar) | L |
| Search: settings + calculator results, top-result highlight | M |
| Quiet mode (local) + QS tile | M |
| Hidden apps with credential lock | S |
| Accessibility pass (200 % font, custom actions, contrast mode) | M |

### Later
| Item | Effort |
| --- | --- |
| Sibling-app contract (Wanda "up next", Phalanx per-room unread) via signature provider | M |
| Hosted third-party `AppWidget` rows | L |
| Quiet mode ↔ Android Modes (`AutomaticZenRule`) | M |
| Contacts in search (`READ_CONTACTS`, opt-in) | M |
| Tablet/foldable centred column & landscape | M |
| Usage-aware long-press sheet (`UsageStatsManager`, opt-in, `ref:ratio/01`) | M |
