# Mono

A minimalist Android home screen: your favourite apps as one vertical list of names, with what is playing shown right under the app playing it. Swipe up for every app, alphabetical, with a letter rail and a search that also finds app shortcuts (such as Phalanx conversations).

Made for GrapheneOS. No network permission, no telemetry, no Google services.

## Features

- Big clock, date and next alarm.
- Favourites in your order; long-press to reorder, remove, hide, see app info, uninstall or open a shortcut.
- Now-playing card under the app that owns the media session, tinted by its artwork.
- Notification dots.
- Work profile apps, badged.
- Search: ignores case and accents, matches initials ("ym" finds YouTube Music); the keyboard's Go key opens the top result.

Music and dots need notification access; Mono asks with a single row on the home screen. On Android 13+ a sideloaded app must first be allowed "restricted settings" from its App info menu.

## Build

```bash
./gradlew :app:assembleDebug
```

## License

[EUPL-1.2](LICENSE)
