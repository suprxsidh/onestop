# OneStop — project constraints & working knowledge

All-in-one Android power-user utility app: Battery + Gestures + System Info, one app instead of three. Kotlin + Jetpack Compose, native only — no Flutter/RN/KMM.

## Build

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` must be exported before every `./gradlew` invocation. Nothing else needs installing.
- Package `com.suprxsidh.onestop`, minSdk 26, targetSdk/compileSdk 34.
- Debug-signed sideload only — no Play Store, no release signing config.
- Single Gradle app module, package-per-feature (`battery/`, `gestures/`, `sysinfo/`) — not a multi-module Gradle setup. See design spec for rationale.

## Status

- **Sub-project 1 (shell + Battery module):** design approved 2026-08-30, spec at `docs/superpowers/specs/2026-08-30-onestop-shell-design.md`, implemented 2026-08-30 on branch `impl/onestop-shell` (all 6 tasks + final whole-branch review + one fix wave, all clean — zero Critical anywhere, zero open Important findings). **Awaiting user decision to merge to `master`** — not merged, not pushed; this is intentional, a merge is a deliberate stop-and-ask point, not something done autonomously. Three items deliberately deferred rather than fixed (see SDD ledger `.superpowers/sdd/2026-08-30-onestop-shell/progress.md` for full rulings): (1) `OneStopDashboardViewModel` is architecturally the "shared ViewModel" the spec forbade — harmless with one tile, fix when Gestures is spec'd; (2) the dashboard's tile-tap navigation test was descoped to manual-verify-only in the spec (no device/emulator was ever available to verify an automated one); (3) the `health` foreground-service type may need on-device confirmation on API 34+ (inherited from BatteryLab, which already runs fine for the user, so likely a non-issue — verify by plugging in the charger after install). Dashboard is functionally correct (real data, dark mode, bounded nav, sparkline rendering) but does not yet pixel-match the approved mockup (no charge ring, custom icons) — visual polish remains a separate follow-up, not yet spec'd.
- **Sub-project 2 (Gestures module):** not yet spec'd.
- **Sub-project 3 (System Info module):** not yet spec'd.

## Related

- BatteryLab (`~/opencode-projects/batterylab`, `suprxsidh/batterylab` on GitHub) — Battery module's source, stays live/untouched as its own app. Code is copied into OneStop, not moved.
- Approved dashboard mockup: https://claude.ai/code/artifact/bd05f1ef-e3f9-4eb3-8354-154ddaeb0cfb
