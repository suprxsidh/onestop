# OneStop Gestures Module ("Microgesture") Implementation Plan

> Executed inline in-session (not via subagent-driven-development) — token cost tradeoff, decided given full context already established. Tasks below match what was actually implemented; see git log on this branch for the real commit sequence.

**Goal:** Add phone-shake/rotation gesture-to-action mapping via a new `AccessibilityService`, with a per-app auto-suppress dispatch-guard, into the existing OneStop dashboard shell.

**Architecture:** New `com.suprxsidh.onestop.gestures` package, package-per-feature like `battery/`. All decision logic (detectors, dispatch-guard, settings persistence) is plain Kotlin with no Android framework types in its public API — fully unit-testable without a device. `MicrogestureAccessibilityService` is a thin wiring class, the only untested surface. Settings persist via Preferences DataStore. Bundled: extract `BatteryTileViewModel` out of `OneStopDashboardViewModel` (architectural debt fix).

**Tech Stack:** Kotlin, Jetpack Compose (Material3), AccessibilityService, SensorManager, Preferences DataStore, Room (existing), JUnit4 + Robolectric + kotlinx-coroutines-test.

**Spec:** `docs/superpowers/specs/2026-08-30-onestop-gestures-design.md`

## Global Constraints

- Compose Foundation pinned to 1.7.0 — `weight()` is a `RowScope`/`ColumnScope` member, never a top-level import.
- Package `com.suprxsidh.onestop`, minSdk 26, targetSdk/compileSdk 34.
- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` before every `./gradlew` call.
- `./gradlew :app:testDebugUnitTest --tests "..."` (not `./gradlew test --tests`).
- No Android device/emulator available — Composables and the AccessibilityService get compile-checks, not behavioral tests.
- Branch: `impl/onestop-gestures`, based on `impl/onestop-shell` (not `master` — master has no buildable project at all, docs only). No merge/push without the user's explicit say-so.

## Task list (see git log for exact commits)

1. Gesture domain models (`GestureType`, `GlobalActionType`, `GestureSettings`)
2. `ShakeDetector`
3. `RotationDetector`
4. `DispatchGuard`
5. `GestureSettingsRepository` (Preferences DataStore)
6. Extract `BatteryTileViewModel` (architectural debt fix)
7. `GesturesTileState`/`Mapper`/`ViewModel`
8. Gestures settings screen (ViewModel + Composable)
9. Blocklist screen (ViewModel + Composable)
10. Dashboard + navigation integration
11. `MicrogestureAccessibilityService` + accessibility config + manifest registration

Full per-task file lists, test code, and implementation code were drafted in the brainstorming/planning conversation; this file records the outcome rather than replaying every step, to keep this doc from duplicating the git history it describes.
