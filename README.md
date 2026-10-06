# CorsixTH Android — Community Modernization Fork

> **Status: active modernization work / not release-ready yet**
>
> This repository is a community-maintained fork of the historical Android port of
> [CorsixTH](https://github.com/CorsixTH/CorsixTH), with the goal of bringing Theme Hospital back to modern Android devices using a maintainable, reproducible and privacy-friendly build.

## Why this fork exists

The original Android port of CorsixTH provided an excellent native way to play Theme Hospital on Android, but its Android-specific integration has fallen behind the current CorsixTH engine.

The objective of this fork is **not** to rewrite CorsixTH from scratch. The objective is to preserve the existing Android work, modernize it carefully, and reconnect it to a recent upstream CorsixTH core.

The initial technical target is:

- **CorsixTH 0.70.1** as the first modern engine baseline;
- **SDL2** for the first modernization cycle;
- **C++17** for the native engine;
- **ARM64 (`arm64-v8a`)** as the primary architecture;
- modern Android support, with Android **14 / 15 / 16** as priority validation targets;
- reproducible local builds on **Windows 11**;
- a build that can work without mandatory Firebase, Google Analytics or Google Play Services;
- preservation of Theme Hospital import, touch controls, audio, saves and existing Android-specific behavior.

CorsixTH 0.70.x is intentionally used as the first target before any SDL3 migration. Moving the engine forward and migrating the multimedia stack at the same time would make regressions much harder to isolate.

---

## Current state

The repository currently still contains the historical Android architecture and has **not yet completed the migration to CorsixTH 0.70.1**.

Current inherited baseline:

- Android Gradle Plugin: **8.7.3**
- Gradle wrapper: **8.9**
- Kotlin: **2.0.21**
- `compileSdk`: **35**
- `targetSdk`: **35**
- `minSdk`: **27** / Android 8.1
- native build: **Android NDK / ndk-build**
- native STL: `c++_static`
- historical CorsixTH Android submodule: `alanwoolley/CorsixTH`, branch `Android`

The modernization work is being developed on:

```text
feature/corsixth-0.70-modernization
```

The default `dev` branch remains the clean fork baseline until the modernization branch reaches defined validation milestones.

---

## Project goals

### 1. Obtain a reproducible baseline build

Before changing the engine, the historical Android project must build locally from a clean checkout with all required submodules.

The first success criterion is deliberately simple:

```text
Clean checkout
      ↓
Submodules initialized
      ↓
Local Android toolchain
      ↓
assembleDebug
      ↓
Installable APK
```

No engine upgrade should be considered validated until this baseline is reproducible.

### 2. Decouple the community build from Google services

The historical project currently integrates Firebase, Crashlytics, Firebase Performance and Google Play Games.

The long-term goal is to provide a community build that can be compiled and used locally without requiring:

- `google-services.json`;
- Firebase Analytics;
- Firebase Crashlytics;
- Firebase Performance;
- Google Play Games;
- Google Play publishing credentials.

A future split may use dedicated build variants such as:

```text
communityDebug
communityRelease
googlePlayRelease
```

The community flavor must remain fully functional without proprietary service credentials.

### 3. Upgrade the CorsixTH core to 0.70.1

The Android wrapper currently relies on a dedicated historical CorsixTH Android branch.

The modernization phase will compare the Android-specific patches against upstream CorsixTH 0.70.1 and progressively port only the required Android adaptations.

Key migration areas include:

- C++11 → **C++17**;
- native source layout changes;
- Lua integration;
- SDL2 integration;
- SDL2_mixer;
- FreeType;
- FFmpeg / movie playback;
- LuaFileSystem / LPeg;
- Android storage integration;
- Android lifecycle integration;
- touch and pointer handling.

### 4. Preserve Android-specific functionality

A successful compile alone is not sufficient.

The following must continue to work:

- first-launch setup;
- Theme Hospital data import;
- GOG installer import where supported;
- demo data workflow where legally available;
- touch controls;
- mouse input;
- audio and music;
- video playback where supported;
- UI scaling;
- save / load;
- application suspend / resume;
- Android back navigation;
- file access on recent Android versions.

### 5. Make Android 16 and ARM64 first-class targets

The first supported modernization target is **ARM64**.

Priority ABI:

```text
arm64-v8a
```

Other ABIs may be retained or restored later if they do not significantly complicate maintenance.

The project will be validated primarily against modern Android storage, lifecycle and permission behavior rather than preserving obsolete Android compatibility at all costs.

---

## Development roadmap

The work is split into controlled phases so that regressions can be attributed to a specific change.

| Phase | Objective | Exit criterion |
|---|---|---|
| **0** | Repository preparation | Branching, documentation and development rules established |
| **1** | Historical local build | Clean checkout produces a debug APK locally |
| **2** | Reproducible toolchain | JDK, Gradle, SDK and NDK versions documented and repeatable |
| **3** | Community build cleanup | Local build no longer requires Google/Firebase credentials |
| **4** | CorsixTH 0.70.1 bootstrap | New core is integrated far enough to compile native code |
| **5** | Native migration | C++17 and native dependencies compile successfully |
| **6** | First launch | Application starts with the 0.70.1-based core |
| **7** | Gameplay validation | Theme Hospital data loads and a playable game can start |
| **8** | Android integration | Touch, storage, lifecycle, audio and save/load are validated |
| **9** | Android 14/15/16 hardening | Modern Android regressions are resolved |
| **10** | Community release | Signed, documented ARM64 release candidate is produced |
| **11+** | Future work | SDL3 and larger architectural modernization, only after stability |

Detailed phase definitions are maintained in:

- [`docs/DEVELOPMENT_PHASES.md`](docs/DEVELOPMENT_PHASES.md)
- [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md)

---

## Development policy

This repository follows a conservative modernization strategy.

Important rules include:

- do not develop directly on `dev`;
- use focused feature branches;
- prefer small, reviewable and reversible commits;
- do not mix engine migration, SDL migration and UI redesign in the same phase;
- do not remove historical Android-specific code until its purpose is understood;
- do not silently change save compatibility;
- do not commit proprietary Theme Hospital assets;
- do not commit signing keys, service credentials or secrets;
- every phase must have an explicit validation criterion before moving forward.

The complete policy is documented in:

- [`DEVELOPMENT_POLICY.md`](DEVELOPMENT_POLICY.md)

Rules intended for AI-assisted development are documented in:

- [`docs/AI_AGENT_RULES.md`](docs/AI_AGENT_RULES.md)

AI agents working on this repository are expected to diagnose one root problem at a time, preserve working behavior, document uncertainty, and avoid broad speculative rewrites.

---

## Build and CI strategy

### Local-first development

This fork is intentionally using a **local-first build strategy** during the modernization phase.

GitHub Actions must **not** be treated as the primary development loop. Frequent compilation and validation are expected to run locally on Windows instead.

The intended development environment is:

```text
Windows 11
   │
   ├─ Git
   ├─ JDK
   ├─ Android SDK
   ├─ Android NDK
   ├─ Gradle wrapper
   ├─ ADB
   └─ optional WSL2 / Docker / act
```

A local GitHub Actions-compatible workflow may later be reproduced with [`act`](https://github.com/nektos/act), but the authoritative requirement is that the project can be built directly with the Gradle wrapper.

Typical target command:

```powershell
.\gradlew.bat assembleDebug
```

### GitHub Actions policy

The upstream repository contains historical GitHub Actions workflows. During modernization, **remote GitHub Actions are not to be enabled, modified or relied upon without explicit project-owner approval**.

This prevents unnecessary CI usage while the project is expected to compile very frequently during migration work.

Once the local build is stable, CI can be introduced as a validation layer rather than as the development environment itself.

---

## What this project is not

This project is **not**:

- a distribution of Theme Hospital;
- a source for proprietary Bullfrog / Electronic Arts assets;
- an APK archive of abandoned commercial software;
- a rewrite of Theme Hospital;
- a replacement for the upstream CorsixTH project;
- currently a stable production release.

The purpose of this repository is to maintain the **Android integration layer** required to run CorsixTH with legally obtained Theme Hospital data.

---

## Theme Hospital data requirement

CorsixTH requires Theme Hospital game data in order to play the full game.

This repository does **not** contain Theme Hospital assets.

Users must provide their own legally obtained game files. Historically the Android port also supported importing supported GOG installers and, where available, using the Theme Hospital demo data.

Modernization work must preserve a clear separation between:

```text
Open-source CorsixTH / Android code
                +
User-provided Theme Hospital data
                =
Playable game
```

---

## Future direction

Once the 0.70.1 / SDL2 Android baseline is stable, possible future work includes:

- migration to newer CorsixTH releases;
- SDL3 evaluation;
- further Android 16+ compatibility work;
- improved touch gestures and phone ergonomics;
- tablet-specific UI improvements;
- optional gamepad improvements;
- simplified Theme Hospital data import;
- cleaner dependency management;
- migration away from legacy `ndk-build` if there is a clear maintenance benefit;
- reproducible release signing and checksums;
- automated local regression tests;
- optional upstreaming of generic Android improvements where appropriate.

These are intentionally secondary goals. **The first priority is a stable, maintainable CorsixTH 0.70.1 Android port.**

---

## Upstream projects and credits

This fork exists because of the work done by the original projects and their contributors.

### CorsixTH

Main engine:

- https://github.com/CorsixTH/CorsixTH

CorsixTH is an open-source reimplementation of the Theme Hospital engine. All CorsixTH contributors deserve credit for the engine, gameplay compatibility and continued development of the project.

### Original Android port

This repository was forked from:

- https://github.com/alanwoolley/CorsixTH-Android

The original Android port contains the Android application layer, setup workflow, native Android integration and mobile-specific adaptations that form the technical foundation of this modernization effort.

Existing contributor information is retained in [`AUTHORS.md`](AUTHORS.md).

### Theme Hospital

Theme Hospital was originally developed by Bullfrog Productions and published by Electronic Arts.

This project is an independent community project and does not distribute the original commercial game data.

---

## Project documentation

Start here if you want to understand or work on the modernization:

- [`DEVELOPMENT_POLICY.md`](DEVELOPMENT_POLICY.md) — development rules and constraints
- [`docs/DEVELOPMENT_PHASES.md`](docs/DEVELOPMENT_PHASES.md) — detailed migration phases
- [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md) — current technical status
- [`docs/MIGRATION_MATRIX_0.70.1.md`](docs/MIGRATION_MATRIX_0.70.1.md) — Android fork vs CorsixTH 0.70.1 delta
- [`docs/AI_AGENT_RULES.md`](docs/AI_AGENT_RULES.md) — rules for AI-assisted development

---

## Contributions

The project is currently in an early modernization phase. Contributions are useful, but changes should follow the documented phase currently in progress rather than introducing unrelated refactors.

Before proposing a large change:

1. read the development policy;
2. check the current project phase;
3. keep the change narrowly scoped;
4. preserve Android-specific behavior unless intentionally replacing it;
5. provide build or runtime evidence whenever possible.

The project favors **measurable progress and reproducibility over large rewrites**.
