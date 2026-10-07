# CorsixTH Android Modernization — Project Status

Last updated: 2026-10-07

## Current branch

`feature/corsixth-0.70-modernization`

Base branch:

`dev`

`dev` is intentionally left untouched while modernization work is validated.

## Current phase

**Phase 7 — Save/load and lifecycle reliability**

Status: **DONE** (device smoke + soft keyboard accepted)

Checkpoint: **`v0.70.1-pre-release`** (APK + bilingual notes)

Previous:

- Phase 0 — DONE
- Phase 1 — DONE (`assembleDebug` + SetupActivity)
- Phase 2 — DONE ([`docs/MIGRATION_MATRIX_0.70.1.md`](MIGRATION_MATRIX_0.70.1.md))
- Phase 3 — DONE ([`docs/PHASE3_NATIVE_TOOLCHAIN.md`](PHASE3_NATIVE_TOOLCHAIN.md))
- Phase 4 — DONE ([`docs/PHASE4_BOOTSTRAP_0.70.1.md`](PHASE4_BOOTSTRAP_0.70.1.md))
- Phase 5 — DONE ([`docs/PHASE5_DATA_PACKAGING.md`](PHASE5_DATA_PACKAGING.md))
- Phase 6 — DONE ([`docs/PHASE6_GAMEPLAY_ESSENTIALS.md`](PHASE6_GAMEPLAY_ESSENTIALS.md))

Next phase:

**Phase 8 — Android 14/15/16 hardening**

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
| Submodule | Phase 4 branch `feature/android-phase4-0.70.1` (0.70.1 + Android glue) |
| Validation build | **SUCCESS**; APK ~43.9 MiB arm64-only |

## Phase 4 summary

Deliverable: [`docs/PHASE4_BOOTSTRAP_0.70.1.md`](PHASE4_BOOTSTRAP_0.70.1.md)

| Item | Result |
|---|---|
| Core | CorsixTH **0.70.1** sources imported |
| Android glue | hooks/events + Lua touch/settings restored |
| Native | `libappmain` builds (lodepng PNG; MIDI device off) |
| Device | GameActivity **Resumed** after `fixConfig` nil-env fix |
| Push | not done (local commits only when requested) |

## Phase 5 summary

Deliverable: [`docs/PHASE5_DATA_PACKAGING.md`](PHASE5_DATA_PACKAGING.md)

| Item | Result |
|---|---|
| `game.zip` | 0.70.1 layout; Bitmap build helpers excluded |
| Engine upgrade | markers + `api_version` **2717** + `.cth_engine_stamp` |
| TH import | SAF persistable; nested-root resolve; post-import validate |
| Setup wizard | confirmed when TH missing |
| Game with TH | GameActivity resumes; data found after restart |

## Phase 6 summary

Deliverable: [`docs/PHASE6_GAMEPLAY_ESSENTIALS.md`](PHASE6_GAMEPLAY_ESSENTIALS.md)

| Item | Result |
|---|---|
| Touch / scroll | Phase 4 synthesis retained |
| Capture mouse | forced off for Android |
| Back button | trapped → Escape/cancel |
| Save hooks | menu-bar swap + save DB update |
| Play / stats | Android events + Play menu restored |
| Device smoke | pending (ADB offline at close) |

## Phase tracking

| Phase | Description | State |
|---|---|---|
| 0 | Baseline/repository control | DONE |
| 1 | Reproducible inherited local build | DONE |
| 2 | Android fork vs 0.70.1 delta analysis | DONE |
| 3 | C++17/native toolchain modernization | DONE |
| 4 | CorsixTH 0.70.1 bootstrap | DONE |
| 5 | Data packaging/import | DONE |
| 6 | Input/UI/audio/gameplay essentials | DONE |
| 7 | Save/load/lifecycle reliability | DONE |
| 8 | Android 14/15/16 hardening | NOT STARTED |
| 9 | Community build cleanup | NOT STARTED |
| 10 | Release engineering | NOT STARTED |
| 11 | SDL3 migration | FUTURE / OUT OF SCOPE |

## Immediate next task

**Phase 8** — Android 14/15/16 hardening (scoped storage, exported components, etc.).

Do **not** enable MIDI or SDL3 yet.

CI: GitHub Actions must use self-hosted runner **GAMING** (`runs-on: [self-hosted, Windows, X64]`).

## Soft keyboard (post Phase 7)

- IME shown for player name / textboxes via `GameActivity` EditText bridge
- Pre-filled `PLAYER` can be deleted (empty-buffer backspace → SDL)
- See [`distribution/pre-release/0.70.1-pre-release.md`](../distribution/pre-release/0.70.1-pre-release.md)

## Documentation control

- `DEVELOPMENT_POLICY.md`
- `docs/DEVELOPMENT_PHASES.md`
- `docs/AI_AGENT_RULES.md`
- `docs/PROJECT_STATUS.md`
- `docs/MIGRATION_MATRIX_0.70.1.md`
- `docs/PHASE3_NATIVE_TOOLCHAIN.md`
- `docs/PHASE4_BOOTSTRAP_0.70.1.md`
- `docs/PHASE5_DATA_PACKAGING.md`
- `docs/PHASE6_GAMEPLAY_ESSENTIALS.md`
- `docs/PHASE7_SAVE_LOAD_LIFECYCLE.md`
- `distribution/pre-release/0.70.1-pre-release.md`
