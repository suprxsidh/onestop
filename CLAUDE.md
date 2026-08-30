# OneStop — project constraints & working knowledge

All-in-one Android power-user utility app: Battery + Gestures + System Info, one app instead of three. Kotlin + Jetpack Compose, native only — no Flutter/RN/KMM.

## Build

- `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` must be exported before every `./gradlew` invocation. Nothing else needs installing.
- Package `com.suprxsidh.onestop`, minSdk 26, targetSdk/compileSdk 34.
- Debug-signed sideload only — no Play Store, no release signing config.
- Single Gradle app module, package-per-feature (`battery/`, `gestures/`, `sysinfo/`) — not a multi-module Gradle setup. See design spec for rationale.

## Status

- **Sub-project 1 (shell + Battery module):** design approved 2026-08-30, spec at `docs/superpowers/specs/2026-08-30-onestop-shell-design.md`, implemented 2026-08-30 on branch `impl/onestop-shell` — all 6 tasks reviewed clean (zero Critical, no per-task Important findings). Final whole-branch review (2026-08-30) found 6 Important cross-task issues invisible at single-task scale — dark mode broken, unbounded nav back-stack, unreliable tile time-window, a spec-mandated nav test silently dropped, a spec §5 architecture violation (shared dashboard ViewModel), a possible foreground-service permission crash needing on-device confirmation. See the SDD ledger (`.superpowers/sdd/2026-08-30-onestop-shell/progress.md`) for full detail and rulings. Not yet merged to `master`, not pushed. Dashboard does not yet pixel-match the approved mockup (no charge ring, custom icons) — visual polish remains a separate follow-up, not yet spec'd.
- **Sub-project 2 (Gestures module):** not yet spec'd.
- **Sub-project 3 (System Info module):** not yet spec'd.

## Related

- BatteryLab (`~/opencode-projects/batterylab`, `suprxsidh/batterylab` on GitHub) — Battery module's source, stays live/untouched as its own app. Code is copied into OneStop, not moved.
- Approved dashboard mockup: https://claude.ai/code/artifact/bd05f1ef-e3f9-4eb3-8354-154ddaeb0cfb
