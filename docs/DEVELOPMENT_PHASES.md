# CorsixTH Android Modernization — Development Phases

This document defines the ordered implementation plan and the exit criteria for each phase.

**Rule:** do not skip a failed phase by compensating with later changes.

## Phase 0 — Baseline and repository control

### Goal

Establish a safe modernization branch and document the inherited state before changing behavior.

### Tasks

- Keep `dev` untouched as the fork baseline.
- Work from `feature/corsixth-0.70-modernization`.
- Inventory current Gradle, SDK, NDK, Kotlin, Java and native build configuration.
- Record current submodules and their exact revisions.
- Record current Android-specific patches in the CorsixTH submodule/fork.
- Inventory existing GitHub workflows without executing them.
- Confirm no proprietary Theme Hospital data exists in the repository.

### Exit criteria

- Development policy exists.
- Phase plan exists.
- AI/agent rules exist.
- Current status document exists.
- Baseline revisions are recorded.
- No GitHub Actions have been triggered intentionally.

---

## Phase 1 — Reproducible local build of the inherited port

### Goal

Build the current fork locally on Windows before changing the CorsixTH core.

### Tasks

- Define the supported local Windows build path.
- Install/pin required JDK.
- Use repository Gradle wrapper.
- Install/pin Android SDK components.
- Install/pin Android NDK.
- Initialize/update submodules.
- Resolve local-only Google/Firebase configuration requirements without committing secrets.
- Build debug APK first.
- Build release-like unsigned/debug-signed APK if practical.
- Install APK using ADB.

### Required evidence

Record:

```text
Windows version:
JDK:
Gradle wrapper:
Android SDK:
Build Tools:
NDK:
ABI:
Command:
Result:
APK path:
Device Android version:
```

### Exit criteria

- Clean clone can produce an APK locally.
- APK installs on at least one ARM64 Android device.
- Application reaches its setup flow or expected first screen.
- Procedure is documented and repeatable.

---

## Phase 2 — Android fork vs CorsixTH 0.70.1 delta analysis

### Goal

Understand exactly what must be ported before replacing the historical Android core.

### Tasks

- Compare current Android CorsixTH fork with upstream CorsixTH 0.70.1.
- Classify Android-specific changes into:
  - bootstrap/startup;
  - file paths/storage;
  - input/touch;
  - logging;
  - SDL lifecycle;
  - audio;
  - graphics;
  - JNI glue;
  - platform-specific macros;
  - Lua hooks;
  - build-only patches.
- Identify upstream changes that make old Android patches obsolete.
- Identify patches that must be forward-ported.

### Deliverable

Create a migration matrix containing:

| Android patch/behavior | Still required? | 0.70.1 equivalent | Migration strategy | Status |
|---|---|---|---|---|

### Exit criteria

- No blind submodule replacement is planned.
- All critical Android-specific behavior has an identified migration path.

---

## Phase 3 — Toolchain and native build modernization

### Goal

Make the Android native toolchain compatible with the 0.70.x requirements before full core migration.

### Tasks

- Move native C++ standard from C++11 to C++17.
- Verify current NDK compatibility.
- Audit `Application.mk` and all `Android.mk` files.
- Confirm `c++_static`/STL strategy.
- Validate ARM64 compilation path.
- Audit include paths and source lists.
- Audit native dependencies individually.
- Keep SDL2.
- Avoid SDL3 migration.

### Exit criteria

- Minimal native build environment supports C++17.
- ARM64 target is explicitly supported.
- Native dependency blockers are known and documented.

---

## Phase 4 — CorsixTH 0.70.1 core bootstrap

### Goal

Compile the Android application with a 0.70.1-based CorsixTH core.

### Strategy

Do not attempt to solve gameplay, input, audio and storage simultaneously.

Priority:

1. compile;
2. link;
3. load native library;
4. initialize SDL;
5. initialize Lua;
6. reach CorsixTH bootstrap;
7. reach visible UI.

### Expected problem categories

- renamed/removed source files;
- changed C++ interfaces;
- generated CMake/config headers;
- Lua API changes;
- dependency discovery differences;
- FFmpeg API/version differences;
- SDL entry-point differences;
- Android file path assumptions.

### Exit criteria

- Android native library builds with the 0.70.1-based core.
- APK installs.
- Application launches without native loader failure.
- CorsixTH initialization reaches a known visible state or a reproducible next blocker.

---

## Phase 5 — Data packaging and Theme Hospital import

### Goal

Restore reliable game-data setup on modern Android.

### Tasks

- Validate `game.zip` asset generation against 0.70.1 layout.
- Validate bundled CorsixTH Lua/resources.
- Preserve separation between open-source engine data and proprietary Theme Hospital data.
- Validate setup wizard.
- Validate folder/file selection using modern Android storage APIs.
- Validate GOG import path if retained.
- Validate demo import/download path if retained.
- Validate application-private storage destination.

### Exit criteria

- Fresh install reaches setup wizard.
- User-owned Theme Hospital data can be imported.
- CorsixTH finds imported data after restart.
- No broad/unnecessary storage permission is required where avoidable.

---

## Phase 6 — Input, UI, audio and gameplay essentials

### Goal

Reach a genuinely playable state.

### Input tests

- tap = left click;
- drag;
- scrolling/map movement;
- long press/right-click equivalent if applicable;
- pinch/zoom where supported;
- Android back behavior;
- hardware mouse;
- keyboard where relevant;
- controller later if practical.

### UI tests

- phone aspect ratios;
- tablet aspect ratios;
- display scaling;
- orientation behavior;
- cutouts/insets if relevant;
- readable dialogs and setup screens.

### Audio tests

- music;
- sound effects;
- pause/resume;
- interruption/resume after Android lifecycle events.

### Gameplay tests

- start new game;
- place/build room;
- hire staff;
- interact with UI;
- run simulation for a meaningful period.

### Exit criteria

- Game is usable without external mouse/keyboard.
- Main audio path works.
- Basic gameplay loop works.

---

## Phase 7 — Save/load and lifecycle reliability

### Goal

Ensure the game survives normal Android usage.

### Tests

1. Start game.
2. Save.
3. Return to launcher/home.
4. Resume.
5. Force-stop application.
6. Relaunch.
7. Load save.
8. Lock/unlock device.
9. Background/foreground repeatedly.

### Additional checks

- save directory persistence;
- upgrade behavior;
- migration from older CorsixTH Android saves if feasible;
- no data loss on application update.

### Exit criteria

- Save/load works after process restart.
- Normal Android lifecycle does not cause routine crashes or silent data loss.

---

## Phase 8 — Android 14/15/16 hardening

### Goal

Remove compatibility debt specific to modern Android.

### Audit areas

- target/compile SDK behavior changes;
- scoped storage;
- foreground/background restrictions;
- package visibility if relevant;
- notification permissions if any remain;
- exported components;
- pending intents;
- file URI/content URI handling;
- native page-size compatibility where applicable;
- edge-to-edge/insets;
- deprecated APIs;
- ARM64-only device behavior.

### Exit criteria

- Functional test on Android 14.
- Functional test on Android 15.
- Functional test on Android 16.
- Known device-specific limitations documented.

---

## Phase 9 — Community build cleanup

### Goal

Decouple core gameplay from unnecessary Google/Firebase services.

### Tasks

Evaluate and progressively isolate/remove:

- Firebase Analytics;
- Firebase Performance;
- Crashlytics;
- Google Services plugin;
- Google Play Games dependency;
- Play Store publication-only code.

Possible future product flavors:

```text
community
playstore
```

The community flavor should be capable of functioning without Google services.

### Exit criteria

- Community APK builds without unnecessary cloud dependencies.
- Core gameplay works offline.
- No telemetry is introduced without explicit owner approval.

---

## Phase 10 — Release engineering

### Goal

Prepare a clean, reproducible, owner-approved package.

### Tasks

- deterministic/documented local build process;
- release signing process documented but secrets external;
- semantic versioning strategy;
- changelog;
- SHA-256 checksums;
- APK naming convention;
- license/attribution audit;
- release notes;
- upgrade test from previous test build.

### GitHub Actions gate

GitHub Actions remain disabled/not used until explicit owner approval.

A future workflow may be designed only after the local build is stable, and it must not be executed without approval.

### Exit criteria

- Owner approves release candidate.
- APK is reproducible through documented local procedure.
- Release does not contain proprietary Theme Hospital data.

---

## Future Phase 11 — SDL3 migration

This is explicitly **not part of the first modernization release**.

It should only begin after the SDL2/0.70.1 Android version is stable.

Likely work areas:

- SDL2 -> SDL3 API migration;
- SDL_mixer changes;
- Android activity/bootstrap changes;
- input event changes;
- rendering/window lifecycle changes;
- updated upstream CorsixTH branch integration.

Treat SDL3 as a separate migration project with its own baseline and regression suite.

---

# Milestone summary

| Phase | Milestone | Required before next? |
|---|---|---|
| 0 | Repository controlled and documented | Yes |
| 1 | Current fork builds locally | Yes |
| 2 | Android/upstream delta understood | Yes |
| 3 | C++17/native toolchain ready | Yes |
| 4 | 0.70.1 core launches | Yes |
| 5 | Game data import works | Yes |
| 6 | Basic gameplay/input/audio works | Yes |
| 7 | Save/load/lifecycle stable | Yes |
| 8 | Android 14-16 hardened | Yes |
| 9 | Community build cleaned | Yes |
| 10 | Release candidate | Final first cycle |
| 11 | SDL3 | Future project |
