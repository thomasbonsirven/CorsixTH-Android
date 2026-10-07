# CorsixTH Android Modernization — Project Status

Last updated: 2026-10-07

## Current branch

`feature/corsixth-0.70-modernization`

Base branch:

`dev`

`dev` is intentionally left untouched while modernization work is validated.

## Current phase

**Phase 9 — Community build cleanup**

Status: **DONE**

Deliverable: [`docs/PHASE9_COMMUNITY_BUILD.md`](PHASE9_COMMUNITY_BUILD.md)

Checkpoint: **`v0.70.1-pre-release`** (Phases 0–7 + soft keyboard)

Previous:

- Phase 0 — DONE
- Phase 1 — DONE
- Phase 2 — DONE ([`docs/MIGRATION_MATRIX_0.70.1.md`](MIGRATION_MATRIX_0.70.1.md))
- Phase 3 — DONE ([`docs/PHASE3_NATIVE_TOOLCHAIN.md`](PHASE3_NATIVE_TOOLCHAIN.md))
- Phase 4 — DONE ([`docs/PHASE4_BOOTSTRAP_0.70.1.md`](PHASE4_BOOTSTRAP_0.70.1.md))
- Phase 5 — DONE ([`docs/PHASE5_DATA_PACKAGING.md`](PHASE5_DATA_PACKAGING.md))
- Phase 6 — DONE ([`docs/PHASE6_GAMEPLAY_ESSENTIALS.md`](PHASE6_GAMEPLAY_ESSENTIALS.md))
- Phase 7 — DONE ([`docs/PHASE7_SAVE_LOAD_LIFECYCLE.md`](PHASE7_SAVE_LOAD_LIFECYCLE.md))
- Phase 8 — DONE ([`docs/PHASE8_ANDROID_HARDENING.md`](PHASE8_ANDROID_HARDENING.md))

Next phase:

**Phase 10 — Release engineering**

## GitHub Actions status

Self-hosted runner **GAMING** (`runs-on: [self-hosted, Windows, X64]`).

Do not trigger workflows without repository-owner approval (except when the owner explicitly requests a release/CI run).

Community CI tasks: `assembleCommunityDebug` / `assembleCommunityRelease`.

## Modernization target

- Core: **CorsixTH 0.70.1**
- ABI priority: **arm64-v8a** (enforced)
- Android targets: **compile/target SDK 36** (Android 16)
- Multimedia: **SDL2** (SDL3 deferred)
- Flavors: **community** (default) / **playstore** (optional)

## Phase 9 summary

| Item | Result |
|---|---|
| Flavors | `community` (default), `playstore` |
| Community deps | no Firebase / Play Games AARs |
| Community plugins | google-services / crashlytics / perf skipped |
| DEX check | no Firebase / Play Games strings |
| Build | `assembleCommunityDebug` SUCCESS |

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
| 8 | Android 14/15/16 hardening | DONE |
| 9 | Community build cleanup | DONE |
| 10 | Release engineering | NOT STARTED |
| 11 | SDL3 migration | FUTURE / OUT OF SCOPE |

## Immediate next task

**Phase 10** — Release engineering (reproducible package, signing docs, checksums, changelog).

Do **not** enable MIDI or SDL3 yet.

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
- `docs/PHASE3_NATIVE_TOOLCHAIN.md` … `docs/PHASE9_COMMUNITY_BUILD.md`
- `distribution/pre-release/0.70.1-pre-release.md`
