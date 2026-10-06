# CorsixTH Android Modernization — Project Status

Last updated: 2026-10-07

## Current branch

`feature/corsixth-0.70-modernization`

Base branch:

`dev`

`dev` is intentionally left untouched while modernization work is validated.

## Current phase

**Phase 3 — Toolchain and native build modernization**

Status: **DONE** (C++17 + ARM64 toolchain validated on historical core; submodule not replaced)

Previous:

- Phase 0 — DONE
- Phase 1 — DONE (`assembleDebug` + SetupActivity)
- Phase 2 — DONE ([`docs/MIGRATION_MATRIX_0.70.1.md`](MIGRATION_MATRIX_0.70.1.md))

Next phase:

**Phase 4 — CorsixTH 0.70.1 core bootstrap**

## GitHub Actions status

**DO NOT RUN without explicit repository-owner approval.**

Local-first validation only. Self-hosted runner `GAMING` must not be used without approval.

## Modernization target

- Core: **CorsixTH 0.70.1**
- ABI priority: **arm64-v8a** (now enforced in build)
- Android targets: 14 / 15 / 16
- Multimedia: **SDL2** (SDL3 deferred)

## Phase 3 summary

Deliverable: [`docs/PHASE3_NATIVE_TOOLCHAIN.md`](PHASE3_NATIVE_TOOLCHAIN.md)

| Item | Result |
|---|---|
| C++17 | `APP_CPPFLAGS` + engine `LOCAL_CPPFLAGS` |
| STL / platform | `c++_static` / `android-27` |
| ABI | **`arm64-v8a` only** (`Application.mk` + `ndk.abiFilters`) |
| NDK | 27.0.12077973 |
| SDL2 | retained (2.30.10) / mixer 2.6.3 |
| PNG strategy | keep lodepng for first 0.70.1 attempt; prefer libpng later |
| MIDI | deferred |
| Submodule | still `fa1ca3e2` (historical Android fork) |
| Validation build | **SUCCESS**; APK ~43.5 MiB arm64-only |

## Phase tracking

| Phase | Description | State |
|---|---|---|
| 0 | Baseline/repository control | DONE |
| 1 | Reproducible inherited local build | DONE |
| 2 | Android fork vs 0.70.1 delta analysis | DONE |
| 3 | C++17/native toolchain modernization | DONE |
| 4 | CorsixTH 0.70.1 bootstrap | NOT STARTED |
| 5 | Data packaging/import | NOT STARTED |
| 6 | Input/UI/audio/gameplay essentials | NOT STARTED |
| 7 | Save/load/lifecycle reliability | NOT STARTED |
| 8 | Android 14/15/16 hardening | NOT STARTED |
| 9 | Community build cleanup | NOT STARTED |
| 10 | Release engineering | NOT STARTED |
| 11 | SDL3 migration | FUTURE / OUT OF SCOPE |

## Immediate next task

Start **Phase 4** carefully:

1. Introduce CorsixTH 0.70.1 sources without discarding Android glue.
2. Update `Android.mk` source list (`th_strings.cpp`, …).
3. Re-apply `Android/` hooks onto 0.70.1 `sdl_core` / `th_lua`.
4. Aim for compile → link → load `libappmain.so` → visible bootstrap.

Do **not** enable MIDI or SDL3 in that first bootstrap.

## Documentation control

- `DEVELOPMENT_POLICY.md`
- `docs/DEVELOPMENT_PHASES.md`
- `docs/AI_AGENT_RULES.md`
- `docs/PROJECT_STATUS.md`
- `docs/MIGRATION_MATRIX_0.70.1.md`
- `docs/PHASE3_NATIVE_TOOLCHAIN.md`
