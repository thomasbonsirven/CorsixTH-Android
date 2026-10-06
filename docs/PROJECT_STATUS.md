# CorsixTH Android Modernization — Project Status

Last initialized: 2026-10-06

## Current branch

`feature/corsixth-0.70-modernization`

Base branch:

`dev`

`dev` is intentionally left untouched while modernization work is validated.

## Current phase

**Phase 0 — Baseline and repository control**

Status: **documentation/bootstrap in progress**

Next phase after Phase 0 closeout:

**Phase 1 — Reproducible local build of the inherited port on Windows**

## GitHub Actions status

**DO NOT RUN without explicit repository-owner approval.**

The inherited workflows are retained as-is for reference only.

Development validation is currently local-first.

## Modernization target

First target core:

`CorsixTH 0.70.1`

First target architecture:

`arm64-v8a`

Primary Android targets:

- Android 16
- Android 15
- Android 14

SDL target for the first modernization cycle:

`SDL2`

SDL3 is deferred to a future independent phase.

## Verified inherited repository state

The following values are inherited from the fork and have been inspected in the modernization branch.

| Component | Current inherited value |
|---|---|
| Default/base branch | `dev` |
| Android Gradle Plugin | `8.7.3` |
| Kotlin | `2.0.21` |
| KSP | `2.0.21-1.0.28` |
| Gradle wrapper | `8.9` |
| `minSdkVersion` | `27` |
| `targetSdkVersion` | `35` |
| `compileSdk` | `35` |
| Native build entry | `jni/Android.mk` via `ndkBuild` |
| STL | `c++_static` |
| NDK platform declaration | `android-27` |
| CorsixTH submodule URL | `https://github.com/alanwoolley/CorsixTH.git` |
| CorsixTH submodule branch hint | `Android` |
| CorsixTH submodule gitlink | `fa1ca3e2d9bbe3721351040bc98812bdf1f0bac0` |
| Firebase/Google plugins | Present in inherited build |
| Google Play Games | Present in inherited build |

## Important inherited build facts

The current Gradle build includes:

- Google Services plugin;
- Firebase Crashlytics;
- Firebase Performance;
- Firebase Analytics;
- Google Play Games v2;
- Room/KSP;
- AndroidX;
- Kotlin/coroutines.

These will **not** be removed during Phase 0/1 unless they prevent establishing the local baseline. They are scheduled for the later community-build cleanup phase.

## Known architectural concern

The Android app does not directly track upstream CorsixTH. It references the dedicated historical Android fork/branch:

`alanwoolley/CorsixTH : Android`

Therefore the migration to CorsixTH 0.70.1 must begin with a delta analysis rather than a direct submodule replacement.

## Phase tracking

| Phase | Description | State |
|---|---|---|
| 0 | Baseline/repository control | IN PROGRESS |
| 1 | Reproducible inherited local build | NOT STARTED |
| 2 | Android fork vs 0.70.1 delta analysis | NOT STARTED |
| 3 | C++17/native toolchain modernization | NOT STARTED |
| 4 | CorsixTH 0.70.1 bootstrap | NOT STARTED |
| 5 | Data packaging/import | NOT STARTED |
| 6 | Input/UI/audio/gameplay essentials | NOT STARTED |
| 7 | Save/load/lifecycle reliability | NOT STARTED |
| 8 | Android 14/15/16 hardening | NOT STARTED |
| 9 | Community build cleanup | NOT STARTED |
| 10 | Release engineering | NOT STARTED |
| 11 | SDL3 migration | FUTURE / OUT OF SCOPE |

## Phase 0 remaining checklist

- [x] Create modernization branch.
- [x] Establish development policy.
- [x] Establish phase gates.
- [x] Establish AI/agent rules.
- [x] Confirm inherited GitHub Action branch triggers before making changes.
- [x] Record major Gradle/Android configuration.
- [x] Record CorsixTH submodule source and current gitlink.
- [ ] Inventory exact local JDK requirement through a clean local build.
- [ ] Determine/pin exact Android NDK version used for the successful baseline.
- [ ] Inventory all native dependency versions/sources.
- [ ] Compare historical Android core against upstream 0.70.1.

The last item may transition directly into Phase 2 after Phase 1 establishes a known-good build.

## Immediate next task

**Do not migrate CorsixTH yet.**

Next engineering task is to establish the inherited project as a reproducible local Windows build and record the exact toolchain that succeeds.

Expected first local commands, after cloning with submodules and preparing required local configuration, will be based around the repository Gradle wrapper rather than a global Gradle installation.

No workflow execution is required for Phase 1.

## Documentation control

Mandatory documents:

- `DEVELOPMENT_POLICY.md`
- `docs/DEVELOPMENT_PHASES.md`
- `docs/AI_AGENT_RULES.md`
- `docs/PROJECT_STATUS.md`

Any AI/developer continuing this work must read these files before implementation.
