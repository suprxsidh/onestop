# OneStop — Gestures Module ("Microgesture") Design Spec

## 1. Purpose

Phone-shake and phone-rotation gesture detection that triggers one of a small set
of built-in system actions (open notifications, back, recents), with a
per-app auto-suppress dispatch-guard so gestures go silent inside apps the user
picks (originally motivated by Super.Money flagging accessibility-enabled apps
as a payment-flow risk) — **without ever toggling the OS-level Accessibility
Service permission**. This is sub-project 2 of OneStop, building on the shell
delivered in sub-project 1 (`impl/onestop-shell`, spec:
`2026-08-30-onestop-shell-design.md`, see its §7 for the original design
insight this module is built around).

## 2. Constraints & context

- Inherits all constraints from the shell spec: Kotlin + Compose (Foundation
  1.7.0 pinned — `weight()` is a `RowScope`/`ColumnScope` member, not a
  top-level import), package-per-feature under `com.suprxsidh.onestop`,
  minSdk 26 / target-compile 34, debug-signed sideload only.
- **No Android device or emulator has ever been available in this
  environment.** Every component here is designed so its actual logic lives in
  plain-Kotlin, unit-testable classes; the `AccessibilityService` subclass
  itself is a thin, largely-untested adapter (Android framework classes
  can't be meaningfully unit tested without Robolectric or a device, neither
  set up here). On-device-only concerns are called out explicitly below, not
  guessed at.
- Enabling the `MicrogestureAccessibilityService` itself still requires the
  one-time standard Android opt-in (Settings → Accessibility) — that is not
  what "no permission toggling" refers to. The insight from shell spec §7 is
  narrower: once the service is on, per-app suppression is handled entirely
  by a dispatch-guard reading the foreground package off `AccessibilityEvent`,
  so the user never has to go back into Settings to disable the service
  before opening a specific app.
- Known architectural debt bundled into this sub-project (per shell spec and
  `CLAUDE.md`): `OneStopDashboardViewModel` currently reaches directly into
  `battery.data.AppDatabase` and `battery.ui.tile.*`. Fixed here by extracting
  `BatteryTileViewModel` + factory so the dashboard composes per-module tile
  view models instead of reaching into feature internals — `GesturesTileViewModel`
  follows the same pattern from day one, so the dashboard never couples to
  `gestures.*` internals either.

## 3. Assumption flagged for review

**"Rotation" gesture = a deliberate quick flip/spin of the device, not a
static portrait/landscape state.** Detecting on orientation state alone would
fire constantly during ordinary phone use (any time the user tilts the phone
to landscape to watch a video, etc.). Instead, `RotationDetector` watches the
gyroscope for a short-duration angular-velocity spike above a threshold —
similar shape to shake detection, different sensor. If this doesn't match
what you had in mind for "rotation," it's a small isolated change to
`RotationDetector` — everything else in this design is unaffected.

## 4. Module architecture

New package `com.suprxsidh.onestop.gestures`, mirroring `battery`'s
package-per-feature split:

- `gestures/detect/` — `ShakeDetector`, `RotationDetector`. Pure Kotlin,
  no Android framework types in their public API (they take raw floats,
  not `SensorEvent`) — fully unit-testable with synthetic data, no device
  needed.
- `gestures/model/` — `GestureType` (SHAKE, ROTATE), `GlobalActionType`
  (BACK, RECENTS, NOTIFICATIONS, NONE), `GestureSettings` data class
  (mapping per gesture type, overall enabled flag, blocklist set of package
  names).
- `gestures/guard/` — `DispatchGuard`: one pure function,
  `resolveAction(foregroundPackage, settings, gestureType): GlobalActionType?`.
  This is the dispatch-guard logic in isolation — the single most
  important thing to unit test thoroughly, and it has zero Android
  framework dependency.
- `gestures/data/` — `GestureSettingsRepository`, backed by Jetpack
  Preferences DataStore (not Room — this is a handful of small values, not
  relational data, and it's process-independent isolation from
  `battery.data.AppDatabase` by construction). Exposes
  `Flow<GestureSettings>` and suspend update functions.
- `gestures/service/` — `MicrogestureAccessibilityService extends
  AccessibilityService`. Wires `SensorManager` → `ShakeDetector`/
  `RotationDetector` → `DispatchGuard.resolveAction()` →
  `performGlobalAction()`. Tracks foreground package from
  `onAccessibilityEvent`. This class is intentionally thin — no branching
  logic beyond calling into the pieces above, so the untestable surface
  is as small as possible.
- `gestures/ui/tile/` — `GesturesTileViewModel` + factory, `GesturesTileState`,
  `GesturesTileMapper` — same shape as the extracted `BatteryTileViewModel`.
- `gestures/ui/settings/` — `GesturesSettingsScreen` (per-gesture action
  picker, master enable/disable), `BlocklistScreen` (installed-app list
  with suppress toggles, via `PackageManager.getInstalledApplications`).
- `res/xml/microgesture_accessibility_config.xml` — declares
  `accessibilityEventTypes="typeWindowStateChanged"` only; no
  `canRetrieveWindowContent`, no gesture-dispatch flags needed since this
  uses `performGlobalAction()`, not `dispatchGesture()`.

## 5. Data flow

1. User enables "Microgesture" in system Accessibility settings once (standard
   Android opt-in for any `AccessibilityService`).
2. `MicrogestureAccessibilityService.onServiceConnected()` registers
   `SensorManager` listeners: accelerometer → `ShakeDetector`,
   gyroscope → `RotationDetector`.
3. Every `TYPE_WINDOW_STATE_CHANGED` event updates the service's
   `currentForegroundPackage`.
4. When a detector emits a gesture signal, the service calls
   `DispatchGuard.resolveAction(currentForegroundPackage, settings, gestureType)`.
   `settings` is the latest value from `GestureSettingsRepository`'s
   `Flow`, cached on the service (collected in a coroutine scope tied to
   `onServiceConnected`/`onDestroy`).
5. If the guard returns non-null, `performGlobalAction(action.toGlobalActionInt())`
   fires. If the foreground package is in the blocklist, or the mapped
   action for that gesture is `NONE`, or the overall enabled flag is off,
   the guard returns `null` and nothing happens — silently, no toast, no log
   visible to the user (this is the whole point: no friction).
6. `GesturesSettingsScreen`, `BlocklistScreen`, and `GesturesTileViewModel`
   all read/write through the same `GestureSettingsRepository` — single
   source of truth, DataStore persists across process death.

## 6. Error handling

- Missing gyroscope (rare, but some low-end/older devices lack one):
  `RotationDetector` reports "unavailable" via a nullable-sensor check at
  registration time; `GesturesTileState` surfaces "Rotation gesture not
  supported on this device" instead of silently doing nothing forever.
  Shake (accelerometer) is present on effectively all Android hardware —
  no fallback needed there.
- Accessibility service not yet enabled by the user: dashboard tile shows
  an "Enable in Settings" affordance that launches
  `Settings.ACTION_ACCESSIBILITY_SETTINGS` (Android does not allow
  programmatically enabling an `AccessibilityService`).
- DataStore read failures: follow the standard Preferences DataStore
  pattern — catch `IOException` in the repository's Flow and emit
  `emptyPreferences()` (falls back to defaults: all gestures unmapped,
  empty blocklist), rethrow anything else.
- `performGlobalAction()` returning `false` (action not supported / no
  matching UI at the OS level right now): no-op, not surfaced to the user
  — matches "no friction" intent; nothing here needs to interrupt them.

## 7. Testing

- `ShakeDetector`, `RotationDetector`: table-driven unit tests with
  synthetic sensor-value sequences (known shake/rotation patterns → detect;
  noise/normal-use patterns → no false positive). No device needed.
- `DispatchGuard.resolveAction()`: the highest-value test target — full
  matrix of (enabled/disabled) × (blocklisted/not) × (mapped action/NONE)
  × (gesture type).
- `GestureSettingsRepository`: unit test against a temp-file-backed
  DataStore instance, verifying round-trip of mapping and blocklist
  updates.
- `GesturesTileViewModel` + `GesturesTileMapper`: same pattern as the
  existing `BatteryTileViewModelTest`, against a fake repository.
- `MicrogestureAccessibilityService` itself: **not unit tested** — it's a
  thin wiring class with no independent logic (everything it calls is
  tested above). Flagged here rather than silently skipped.
- No instrumented/on-device tests in this sub-project — no device or
  emulator available. The user should sideload-install and manually verify:
  service enable flow, shake/rotation firing the right action, and that
  the Super.Money (or whichever app they add) suppression actually goes
  silent.

## 8. Out of scope (this spec)

- Custom/arbitrary actions beyond BACK / RECENTS / NOTIFICATIONS (flashlight,
  media control, launch-app — none were selected for v1).
- Auto-detecting "sensitive" apps to suggest for the blocklist — v1 is
  fully manual (user picks apps in `BlocklistScreen`).
- System Info module (sub-project 3, after this one).
- Dashboard visual polish (existing "known gap" from sub-project 1).
- Actual merge of `impl/onestop-shell` (or this module's branch) into
  `master`, or any push to a remote — both remain the user's explicit call,
  not auto-performed.
