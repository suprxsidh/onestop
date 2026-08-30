# OneStop — Shell & Battery Module Design Spec

**Date:** 2026-08-30
**Status:** Approved (design), pending implementation plan
**Owner:** Suprasidh
**Repo target:** GitHub `suprxsidh/onestop` (personal, push under `suprxsidh`) — new repo, created when implementation starts, not yet created.

## 1. Purpose

OneStop is an all-in-one Android power-user utility app, merging three previously-separate ideas into one app with one icon:

- **Battery** — charge/discharge analytics (already built and shipped separately as [BatteryLab](https://github.com/suprxsidh/batterylab); rehoused here as the first module).
- **Gestures** — phone-shake/rotation gesture-to-action mapping (working name "Microgesture"; not yet built — separate spec).
- **System Info** — GCPU/CPU-Z-style live hardware/sensor internals viewer (not yet built — separate spec).

Driving motivation: no reason to have three phone-utility apps when one will do, especially once Gestures needs to know what app is in the foreground (see §7) — that's naturally a shell-level concern, not a per-app one.

**This spec covers sub-project 1 of 3 only: the shell app and module framework, with Battery rehoused as the first real module.** Gestures and System Info are stubbed placeholders here; each gets its own spec before implementation.

## 2. Constraints & context

- **Platform:** Android phone (Suprasidh's phone, same as BatteryLab).
- **Distribution:** sideload debug-signed APK via `adb install`. No Play Store, no release signing.
- **Language/framework:** Kotlin + Jetpack Compose, native only — no Flutter/RN/KMM (matches BatteryLab).
- **Package:** `com.suprxsidh.onestop`, minSdk 26, targetSdk/compileSdk 34.
- **Build env:** identical to BatteryLab — `JAVA_HOME=/opt/homebrew/opt/openjdk@17`, `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`, nothing new to install.
- **Repo identity:** BatteryLab's existing repo (`suprxsidh/batterylab`) stays live and untouched — it has a public README with a direct APK download link, so nothing about it breaks. OneStop is a new, separate repo; BatteryLab's code is *copied* into it, not moved.

## 3. Module architecture

**Single Gradle app module, package-per-feature** — not a true multi-module Gradle setup with separate `:feature:*` modules.

- BatteryLab's existing code moves in near-verbatim under `com.suprxsidh.onestop.battery.*`, preserving its current internal layout (`calc/`, `health/`, service classes) and its existing test suite (same assertions, new import paths).
- Gestures and System Info will later land in sibling packages the same way (`com.suprxsidh.onestop.gestures.*`, `com.suprxsidh.onestop.sysinfo.*`), once their own specs land.
- **Why not full multi-module:** this is a solo-maintained FOSS utility app at BatteryLab's current scale. Hard Gradle module boundaries buy compile-time isolation and faster incremental builds, at real upfront boilerplate cost. Not worth it yet — package boundaries are enough discipline, and it's a straightforward later split if the project outgrows it.

## 4. UI / navigation design

**Dashboard of tiles**, not a persistent bottom-nav bar. Home screen shows a grid of module tiles, each with a live at-a-glance stat; tapping a tile drills into that module's full screen(s). Chosen over bottom-nav because it matches the "toolbox" feel (GCPU/CPU-Z vibe) better than "one continuous app."

Visual direction: Android Material 3 Expressive (Android 16's actual shipped 2026 design language) — bolder expressive typography, varied/asymmetric corner shapes rather than uniform rounded rectangles, tonal elevation instead of flat drop-shadow cards. Explicitly avoiding generic AI-app-template defaults (settings-list rows as primary layout, purple-gradient hero banners, generic icon-library glyphs).

Approved mockup (light + dark): https://claude.ai/code/artifact/bd05f1ef-e3f9-4eb3-8354-154ddaeb0cfb

- **Battery tile** — hero tile (larger, warm-tinted card): charge ring, time-remaining, peak watts, discharge sparkline.
- **Gestures tile** and **System Info tile** — smaller paired row below the hero tile. In this sub-project these render as placeholder/disabled state (no live data yet — their modules don't exist).

## 5. Data flow

- Each tile owns its own lightweight state holder (ViewModel/StateFlow); the dashboard Composable collects three independent state flows and renders tiles. No shared "dashboard ViewModel" pulling everything centrally — keeps modules decoupled, consistent with the package-boundary decision in §3.
- Battery's tile state holder wraps BatteryLab's existing repository/aggregator classes as-is, exposing a small "at-a-glance" projection (charge %, time-left, peak watts, last-N samples for the sparkline) rather than its full detail-screen state.
- Tapping the Battery tile navigates into BatteryLab's existing detail screens, reused as-is.

## 6. Testing

- BatteryLab's existing unit tests (`calc/`, `health/` packages) migrate over verbatim — same assertions, new package/import paths.
- New tests needed for this sub-project: the battery-detail-state → tile-projection mapping function (done — `BatteryTileMapperTest`, 7 cases).
- **Amended post-implementation (final review, 2026-08-30):** dashboard tile-tap navigation was originally specced as a test requirement but never implemented — no `androidTest` source set exists anywhere in this project, and no Android device or emulator was available at any point during implementation to verify one would actually run. Rather than leave this silently dropped, it's explicitly descoped here: dashboard rendering and navigation are **manual-verify-only** for this sub-project. Before trusting the dashboard, install the APK on a real device and confirm: (a) the Battery tile shows live data and tapping it opens the Battery module's 5-tab screen, (b) the back button returns cleanly to the dashboard, (c) both light and dark system theme settings render correctly (this was a real bug, since fixed — see below). Adding real Compose UI test coverage (`ui-test-junit4` + `createAndroidComposeRule`) is a fair follow-up once a device/emulator is available to verify the test itself works, not before.

## 7. Per-app gesture suppression (context for future Gestures spec, not built here)

Noted here because it shapes why Gestures belongs in this shell rather than as a standalone app: the real pain point driving this whole project is having to manually disable Microgesture's Accessibility Service every time before opening Super.Money (Suprasidh's UPI app), because Android flags accessibility-enabled apps as a screen-reading risk during payment flows.

Key insight for the future Gestures spec: this does **not** require toggling the OS-level Accessibility Service permission at all. An `AccessibilityService` already receives the foreground app's package name on every window-state event, so "off inside Super.Money" can just be a gesture-dispatch guard checking a user-editable app blocklist — no permission toggling, and it generalizes to "any app" for free. This is why Gestures needs to live in the same app/process as everything else long-term, even though it's spec'd separately.

## 8. Out of scope (this spec)

- Gestures module implementation (own spec, next).
- System Info module implementation (own spec, after Gestures).
- Actual GitHub repo creation / first push (happens at implementation start, not during spec-writing).
- BatteryLab repo deprecation/sunset — no decision made; it stays live indefinitely until/unless revisited.
