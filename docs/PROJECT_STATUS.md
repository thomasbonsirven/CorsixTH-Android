# CorsixTH Android Modernization — Project Status

Last updated: 2026-10-06

## Current branch

`feature/corsixth-0.70-modernization`

Base branch:

`dev`

`dev` is intentionally left untouched while modernization work is validated.

## Current phase

**Phase 2 — Android fork vs CorsixTH 0.70.1 delta analysis**

Status: **DONE** (migration matrix written; no core replacement)

Previous:

- **Phase 0** — repository control / docs — DONE
- **Phase 1** — reproducible local Windows build + device launch to SetupActivity — DONE

Next phase:

**Phase 3 — Toolchain and native build modernization for 0.70.1 acceptance**

## GitHub Actions status

**DO NOT RUN without explicit repository-owner approval.**

Local-first validation only. Self-hosted runner `GAMING` must not be used without approval.

## Modernization target

- Core: **CorsixTH 0.70.1**
- ABI priority: **arm64-v8a**
- Android targets: 14 / 15 / 16
- Multimedia: **SDL2** (SDL3 deferred)

## Phase 1 — verified Windows + device baseline

| Item | Verified value |
|---|---|
| Host OS | Windows 11 Professionnel (`10.0.26200`) |
| Branch | `feature/corsixth-0.70-modernization` |
| CorsixTH submodule | `fa1ca3e2` (`alanwoolley/CorsixTH`) |
| JDK | Temurin **17.0.14+7** |
| Gradle / AGP / Kotlin | **8.9** / **8.7.3** / **2.0.21** |
| SDK | `E:\000-lastBeacon_ia\android-sdk` (Platform 35, Build-Tools 35.0.1) |
| NDK | **27.0.12077973** (pinned via `android.ndkVersion`) |
| `assembleDebug` | **SUCCESS** (~8m 43s cold native) |
| APK | `build/outputs/apk/debug/CorsixTH-Android-debug.apk` (~76 MiB) |
| ABIs | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |
| Device | Nubia NX789J, Android 16 |
| Runtime | SetupActivity reached; community build no longer crashes |

### Community runtime crash (fixed)

DropBox root cause:

```text
IllegalArgumentException: Please set a valid API key
  at FirebaseInstallations / Firebase Performance TransportManager
```

Fix: disable Firebase Performance + Play Games init when community `google-services` stub is detected (`BuildConfig.COMMUNITY_BUILD`).

## Phase 2 — delta analysis summary

Deliverable: [`docs/MIGRATION_MATRIX_0.70.1.md`](MIGRATION_MATRIX_0.70.1.md)

| Item | Result |
|---|---|
| Upstream compared | `CorsixTH/CorsixTH` **v0.70.1** (`56bd5d00`) |
| Android submodule | `fa1ca3e2` |
| Approx. merge-base | `fc38c4f4` (near **0.67** release bump) |
| Divergence | ~157 Android-only commits vs ~1235 upstream commits |
| Blind submodule replace? | **No** |
| Critical Android glue | `Android/androidhooks.*`, `Android/androidevents.*`, `sdl_core` hook, Lua `TH.android*`, menus/touch, `appmain.cpp` |
| Already C++17? | **Yes** in `jni/CorsixTH/Android.mk` (`-std=c++17`) |
| First deferrals | MIDI/FluidSynth stack; full Play Games/achievements; SDL3 |

## Inherited configuration (still accurate)

| Component | Value |
|---|---|
| Default/base branch | `dev` |
| `minSdk` / `targetSdk` / `compileSdk` | 27 / 35 / 35 |
| Native entry | `jni/Android.mk` via `ndkBuild` |
| STL / NDK platform | `c++_static` / `android-27` |
| CorsixTH submodule URL | `https://github.com/alanwoolley/CorsixTH.git` (branch hint `Android`) |

## Phase tracking

| Phase | Description | State |
|---|---|---|
| 0 | Baseline/repository control | DONE |
| 1 | Reproducible inherited local build | DONE |
| 2 | Android fork vs 0.70.1 delta analysis | DONE |
| 3 | C++17/native toolchain modernization | NOT STARTED |
| 4 | CorsixTH 0.70.1 bootstrap | NOT STARTED |
| 5 | Data packaging/import | NOT STARTED |
| 6 | Input/UI/audio/gameplay essentials | NOT STARTED |
| 7 | Save/load/lifecycle reliability | NOT STARTED |
| 8 | Android 14/15/16 hardening | NOT STARTED |
| 9 | Community build cleanup | NOT STARTED |
| 10 | Release engineering | NOT STARTED |
| 11 | SDL3 migration | FUTURE / OUT OF SCOPE |

## Immediate next task

Start **Phase 3** only:

1. Extend/adapt `Android.mk` + Android `config.h` so 0.70.1 sources can be accepted.
2. Decide PNG strategy (upstream libpng vs current lodepng).
3. Keep SDL2; do not enable MIDI yet.
4. Do **not** replace the submodule until a compile plan from the matrix is ready.

No GitHub Actions. No push unless owner requests it.

## Documentation control

Mandatory documents:

- `DEVELOPMENT_POLICY.md`
- `docs/DEVELOPMENT_PHASES.md`
- `docs/AI_AGENT_RULES.md`
- `docs/PROJECT_STATUS.md`
- `docs/MIGRATION_MATRIX_0.70.1.md` (Phase 2 deliverable)
