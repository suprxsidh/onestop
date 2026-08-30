# OneStop — project constraints & working knowledge

All-in-one Android power-user utility app: Battery + Gestures + System Info, one app instead of three. Kotlin + Jetpack Compose, native only — no Flutter/RN/KMM.

## Build

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` must be exported before every `./gradlew` invocation. Nothing else needs installing.
- Package `com.suprxsidh.onestop`, minSdk 26, targetSdk/compileSdk 34.
- Debug-signed sideload only — no Play Store, no release signing config.
- Single Gradle app module, package-per-feature (`battery/`, `gestures/`, `sysinfo/`) — not a multi-module Gradle setup. See design spec for rationale.

## Status

- **Sub-project 1 (shell + Battery module):** design approved 2026-08-30, spec at `docs/superpowers/specs/2026-08-30-onestop-shell-design.md`, implemented 2026-08-30 (6/6 tasks done on branch `impl/onestop-shell`, not yet merged to `master`). Dashboard is functionally wired (real data via `BatteryTileMapper`, theme colors match approved palette) but does not yet pixel-match the approved mockup — no charge ring, no sparkline rendering, no custom icons; visual polish is a separate follow-up task, not yet spec'd.
- **Sub-project 2 (Gestures module):** not yet spec'd.
- **Sub-project 3 (System Info module):** not yet spec'd.

## Related

- BatteryLab (`~/opencode-projects/batterylab`, `suprxsidh/batterylab` on GitHub) — Battery module's source, stays live/untouched as its own app. Code is copied into OneStop, not moved.
- Approved dashboard mockup: https://claude.ai/code/artifact/bd05f1ef-e3f9-4eb3-8354-154ddaeb0cfb
