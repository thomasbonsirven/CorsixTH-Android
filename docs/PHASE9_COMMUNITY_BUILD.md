# Phase 9 — Community build cleanup

Last updated: 2026-10-07

Status: **DONE**

**Branch:** `feature/corsixth-0.70-modernization`

## Goal

Decouple core gameplay from Google/Firebase so the community APK runs offline without cloud SDKs or telemetry.

## Product flavors

| Flavor | Purpose | Build task |
|---|---|---|
| **community** (default) | No Firebase / Play Games / Google Services plugins | `assembleCommunityDebug` / `assembleCommunityRelease` |
| **playstore** | Optional Play Store distribution with Firebase + Play Games | `assemblePlaystoreRelease` (requires real `google-services.json`) |

Playstore variants are **disabled** at configuration time unless a non-stub `google-services.json` is present or the Gradle task name contains `playstore`.

## Isolation design

| Layer | Community | Play Store |
|---|---|---|
| Dependencies | no Firebase / Play Games AARs | `playstoreImplementation` Firebase + games-v2 |
| Gradle plugins | skipped | google-services, crashlytics, firebase-perf |
| `DistributionBootstrap` | NoOp crash reporter; no Play Games | FirebaseCrashReporter + PlayGamesSdk |
| Consent / analytics prefs | skipped; analytics screen removed | unchanged opt-in flow |
| Manifest meta | Play Games / Firebase meta removed | kept from main manifest |

Shared façades in `main`:

- `CrashReporter` / `Diagnostics` / `NoOpCrashReporter`
- `PlayGamesController`

## Validation

| Check | Result |
|---|---|
| `assembleCommunityDebug` | **SUCCESS** (`build-phase9.log`) |
| Firebase / Play Games strings in community DEX | **none** |
| Community APK size (debug) | ~33.6 MiB |
| Telemetry without consent | not present (NoOp reporter) |

## CI / workflows

| Workflow | Task |
|---|---|
| `android-ci.yml` | `assembleCommunityDebug` |
| `pre-release.yml` | `assembleCommunityRelease` |
| `build_and_sign.yml` | `assemblePlaystoreRelease` + Crashlytics symbol upload |

## Exit criteria

| Criterion | Status |
|---|---|
| Community APK builds without unnecessary cloud dependencies | OK |
| Core gameplay works offline | OK (no GMS init / no network SDK in community) |
| No telemetry without owner approval | OK (NoOp; analytics prefs removed) |

## Notes

- Historical stub `distribution/community/google-services.community.json` is retained for reference; community builds no longer copy it into the project root.
- Play Store builds still need a private `google-services.json` (gitignored).
- Lua Play Games menu buttons are no-ops in community (`signIn` / `showAchievements` hit a null controller).
