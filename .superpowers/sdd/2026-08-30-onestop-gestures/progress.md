# OneStop Gestures — implementation progress

Executed inline in-session (not via subagent-driven-development — token-cost
tradeoff, decided given full context already established from brainstorming).
All 11 tasks from `docs/superpowers/plans/2026-08-30-onestop-gestures.md`
complete on branch `impl/onestop-gestures` (based on `impl/onestop-shell`,
not `master` — `master` has no buildable project, docs only).

## Result

- 84/84 unit tests pass (`./gradlew clean :app:testDebugUnitTest`) — 53
  pre-existing (sub-project 1) + 31 new.
- `./gradlew assembleDebug` succeeds; debug APK at
  `app/build/outputs/apk/debug/app-debug.apk`.
- No merge to `master`, no push anywhere — both remain the user's call.

## Bugs found and fixed during implementation (not in the original plan)

1. **DataStore test-isolation leak**: `GestureSettingsRepository`'s backing
   file (`"gesture_settings"`) is a fixed singleton path; Robolectric does
   not guarantee a fresh files directory per test method within the same
   class/JVM run, so state leaked between test methods. Fixed by adding
   `GestureSettingsRepository.clear()` and calling it in `@Before` for every
   test that constructs a repository against
   `ApplicationProvider.getApplicationContext()`.
2. **`BlocklistViewModel.setSuppressed` hang**: the original implementation
   did an async read (`repository.settings.first()`) then an async write
   inside one `viewModelScope.launch`, which hung indefinitely under
   `UnconfinedTestDispatcher` + real DataStore IO. Fixed by reading the
   already-cached `StateFlow` value synchronously instead of re-fetching —
   matches the single-suspend-call shape used successfully elsewhere
   (`GesturesSettingsViewModel`).

Both fixes are in production code (`GestureSettingsRepository.clear()`,
`BlocklistViewModel`), not test-only workarounds.

## What is NOT verified (no Android device/emulator available)

- The dashboard renders as intended (Gestures tile, layout, spacing).
- The Gestures settings screen and blocklist screen render/behave correctly
  (dropdowns, switches, list scrolling).
- The `MicrogestureAccessibilityService` actually detects shake/rotation and
  dispatches the right system action.
- Whether the "rotation = deliberate flip via gyroscope" assumption (spec §3)
  matches what the user actually wants — flagged for their review.
- The per-app suppression (blocklist) genuinely silences gestures in a real
  app like Super.Money.

See spec §7 and the manual verification checklist in the plan (Task 11,
Step 3) for exactly what to check after sideloading.
