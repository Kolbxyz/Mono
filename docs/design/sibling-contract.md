# Sibling-app contract: Wanda and Phalanx ↔ Mono

Status: **Mono side implemented** (§5); Wanda and Phalanx sides are still proposals. This document
fixes names and shapes so the three apps can be changed independently.

Spec reference: `ui-ux-spec.md` §4.4 ("Sibling-app contract") and the *Later* table.

## 1. Goals and non-goals

- Wanda publishes **"up next" in true playback order** (the platform queue Mono reads today is in
  timeline order, so it is wrong with shuffle on) and a **local library search** for Mono's drawer.
- Phalanx publishes **unread per room**, including rooms whose notifications are muted (which the
  notification listener never sees).
- Mono works fully without either app, and without the contract on either side.
- No network anywhere: no provider query may start a sync, a remote search or a fetch.
- No message content leaves Phalanx: counts, room names and a deep link only.

## 2. Mechanism

One read-only `ContentProvider` per sibling, guarded by a **signature-level permission**. Mono reads
with `ContentResolver.query` and listens with `ContentObserver`; the siblings call
`notifyChange` when their data changes. No polling, no broadcasts, no bound services.

### 2.1 Permission

```xml
<!-- Declared identically in Wanda, Phalanx and Mono (each may be installed first). -->
<permission
    android:name="com.wander.permission.SIBLING_READ"
    android:protectionLevel="signature"
    android:label="@string/sibling_permission_label" />
<!-- Mono only -->
<uses-permission android:name="com.wander.permission.SIBLING_READ" />
```

`signature` means the system grants it only to apps signed with the **same key**, silently, with
no prompt. All three release builds must therefore share the release signing key.

Debug builds declare and use `com.wander.permission.SIBLING_READ_DEBUG` instead (a manifest
placeholder set per build type). Android refuses to install an app that redeclares a permission
already owned by an app signed with another key (`INSTALL_FAILED_DUPLICATE_PERMISSION`), so a
debug build sharing the release name could not be installed next to a release sibling.

Debug builds: Wanda pins a project-local `debug.keystore`; Phalanx and Mono use AGP's per-machine
default. For the contract to work in debug, Mono and Phalanx must pin the same `debug.keystore` as
Wanda (same `signingConfigs.debug` block Wanda has). With the separate debug permission name, mismatched
debug keys between debug builds still block the install of the second one, so pinning the same
key is required, not optional.

### 2.2 Authorities

| App | Release authority | Debug authority (`${applicationId}.mono`) |
| --- | --- | --- |
| Wanda | `com.wander.android.mono` | `com.wander.android.debug.mono` |
| Phalanx | `org.phalanx.android.mono` | `org.phalanx.android.debug.mono` |

Mono picks the authority matching the installed package (`com.wander.android` or
`com.wander.android.debug`, etc.) and checks it with `packageManager.resolveContentProvider`.

### 2.3 Versioning

Every cursor carries `Bundle` extras `contract_version` (Int, starts at `1`). Columns are only ever
added; a removed or renamed column is a new path (`/v2/...`). Mono reads columns by name with
`getColumnIndex` and ignores unknown ones.

## 3. Wanda

### 3.1 `content://com.wander.android.mono/up_next`

The items that will play after the current one, **in playback order** (shuffle and repeat applied),
at most `limit` (query parameter, default 3, max 10). Empty cursor when nothing is queued.

| Column | Type | Meaning |
| --- | --- | --- |
| `_id` | Long | Position in the up-next list, 0 = next |
| `queue_id` | Long | Media3 media item index. Equals the platform `QueueItem.queueId` Media3 publishes, so Mono can keep using `transportControls.skipToQueueItem(queue_id)` |
| `media_id` | String | `MediaItem.mediaId` |
| `title` | String | |
| `artist` | String? | |
| `album` | String? | |
| `duration_ms` | Long | `-1` when unknown |
| `artwork_uri` | String? | `content://com.wander.android.mono/artwork/<media_id>`, served by `openFile` from Wanda's image cache only; null when not cached (never fetched for Mono) |

`notifyChange(up_next)` on `onTimelineChanged`, `onMediaItemTransition`,
`onShuffleModeEnabledChanged`, `onRepeatModeChanged`.

### 3.2 `content://com.wander.android.mono/search?q=<text>&limit=<n>`

Local library only (`source != 'UNRESOLVED'`, same matching as `TrackLibraryQueries.searchTracks`),
ordered by play count, `limit` default 5, max 20. Never runs a backend search.

| Column | Type | Meaning |
| --- | --- | --- |
| `_id` | Long | Row number |
| `track_id` | String | `tracks.id` |
| `title` | String | |
| `artist` | String | |
| `album` | String? | |
| `duration_ms` | Long | |
| `play_uri` | String | `wanda://track?...` built by `UniversalTrackLink`, which `AppIntentHandler` already opens |

### 3.3 Code changes in Wanda

1. `core/playback/UpNextSnapshot.kt`: `@Singleton` holding `StateFlow<List<UpNextItem>>`.
   `PlaybackService.onCreate` adds a `Player.Listener` that rebuilds it by walking
   `player.currentTimeline.getNextWindowIndex(index, player.repeatMode, player.shuffleModeEnabled)`
   from `currentMediaItemIndex` (up to 10 steps, stopping on `C.INDEX_UNSET` or a repeat back to the
   current index), then calls `contentResolver.notifyChange(UpNextContract.UP_NEXT, null)`.
   `PlaybackService` stays unexported; the provider never touches the player.
2. `core/database/dao/TrackLibraryQueries.kt`: a Cursor-returning twin of `searchTracks` (Room
   supports `Cursor` return types), so the provider needs no `runBlocking`:
   `@Query(<same SQL, selected columns>) fun searchTracksCursor(query: String, limit: Int): Cursor`.
3. `mono/SiblingProvider.kt` (new package): `ContentProvider` with a `UriMatcher` for `up_next`,
   `search`, `artwork/*`; `query` builds a `MatrixCursor` from `UpNextSnapshot` or wraps the Room
   cursor in a `CursorWrapper` that renames columns and adds `play_uri`; `openFile` serves cached
   artwork read-only; `insert`/`update`/`delete` throw `UnsupportedOperationException`. Hilt access
   through an `@EntryPoint` (providers are created before `Application.onCreate`).
4. `mono/UpNextContract.kt`: authority, paths and column names as constants (mirrored in Mono).
5. `AndroidManifest.xml`: the `<permission>` above and
   ```xml
   <provider
       android:name=".mono.SiblingProvider"
       android:authorities="${applicationId}.mono"
       android:exported="true"
       android:readPermission="com.wander.permission.SIBLING_READ"
       android:writePermission="com.wander.permission.SIBLING_READ" />
   ```
6. Tests: the next-index walk (shuffle, repeat one/all, end of queue) as a pure function.

## 4. Phalanx

### 4.1 `content://org.phalanx.android.mono/rooms`

Rooms with something unread (`unread_count > 0 || is_marked_unread`), muted rooms **included**,
ordered by `last_activity_ms` descending, at most `limit` (default 20). Low-priority (archived)
rooms excluded.

| Column | Type | Meaning |
| --- | --- | --- |
| `_id` | Long | Row number |
| `room_id` | String | Matrix room ID |
| `name` | String | Display name as Phalanx shows it |
| `is_direct` | Int (0/1) | |
| `unread_count` | Long | `RoomSummary.unreadCount` |
| `highlight_count` | Long | Mentions |
| `is_muted` | Int (0/1) | `RoomSummary.isMuted` |
| `is_marked_unread` | Int (0/1) | |
| `last_activity_ms` | Long | `-1` when unknown |

Cursor extras: `contract_version`, `snapshot_at_ms` (when Phalanx last saw sync state; Mono may
show nothing when it is older than a day rather than stale counts).

Opening a room: Mono launches Phalanx's launcher activity with
`putExtra("org.phalanx.android.ROOM_ID", room_id)` (`Notifier.EXTRA_ROOM_ID`, already handled by
`MainActivity.handleIntent`). No new intent filter is needed.

### 4.2 Code changes in Phalanx

1. `core/mono/UnreadSnapshot.kt`: collects `RoomListRepository.rooms` on the application scope,
   maps to the columns above (no `preview`, no sender, no avatar), writes a small file in
   `noBackupFilesDir` (so the provider can answer after process death without starting the SDK),
   and calls `notifyChange(rooms)`. Writes are debounced to one per second during initial sync.
2. `core/mono/SiblingProvider.kt`: read-only `MatrixCursor` from that snapshot. Must never touch
   `ActiveClient` or start sync: a provider query can cold-start the process.
3. `core/mono/MonoContract.kt`: authority, path, column constants.
4. `AndroidManifest.xml`: the `<permission>` and a `<provider>` like Wanda's, with
   `android:authorities="${applicationId}.mono"`.
5. Respect Phalanx's own privacy: while `core/security/AppLock` is enabled, publish counts with
   `name` set to an empty string; on logout, delete the snapshot and `notifyChange`.
6. Tests: snapshot mapping (filtering, ordering, muted rooms kept).

## 5. Mono side (implemented)

- Manifest: the `<permission>` and `<uses-permission>` of §2.1, and one
  `<queries><provider android:authorities="…" />` per authority of §2.2 (release and debug).
- `core/siblings/SiblingProviders.kt`: checks `checkSelfPermission(SIBLING_READ)` and resolves the
  first authority that answers (`resolveContentProvider`), release package before debug. Reads run on
  `Dispatchers.IO`; a `SecurityException` (other signing key) means "absent". Observed paths re-read
  on `ContentObserver.onChange`, conflated; nothing is polled.
- `core/siblings/SiblingRows.kt`: cursor rows to models by column name. Rows missing a required
  column (`queue_id`/`title`; `track_id`/`title`/`play_uri`; `room_id`) are dropped.
- `core/siblings/WandaUpNext.kt`: used **only when the playing session is Wanda and its own queue
  gives no up-next** (the session queue still wins when it has one). Only the Wanda build that is
  playing is asked. Tapping an item still calls `skipToQueueItem(queue_id)`.
- `core/siblings/WandaSearch.kt`: drawer search asks `search?q=&limit=20` per query; at most 3
  songs are shown, in Wanda's order, after contacts and before settings pages. Tapping sends
  `ACTION_VIEW` on `play_uri` with `setPackage(<the Wanda build that answered>)`.
- `core/siblings/PhalanxRooms.kt`: per-room counts go on Phalanx's **conversation shortcut search
  hits** (Phalanx's shortcut id is the room id), replacing the notification-derived count for that
  package in Mono's own profile. A room absent from the cursor has no badge; `is_marked_unread`
  without a count is a dot. No separate unread row and no dimming of muted rooms (not needed for
  a badge). When `snapshot_at_ms` is older than a day, Mono ignores the snapshot and falls back to
  notification counts.
- All flows are collected only while home or the drawer is watched (`WhileSubscribed`), and the
  provider is re-resolved each time collection starts, so installing a sibling is picked up the next
  time home is shown.
- Debug builds use the separate `SIBLING_READ_DEBUG` permission (§2.1), so a debug Mono installs
  next to release siblings and simply sees the contract absent.

## 6. Security notes

- `exported="true"` is required for cross-app access; the signature permission is the guard. Both
  read and write permissions are set, and all write methods throw.
- Selection/sort arguments from callers are ignored; only the documented query parameters are
  read, and `q` is bound as a Room parameter, never concatenated.
- Phalanx's provider exposes no message bodies, senders, avatars or keys.
