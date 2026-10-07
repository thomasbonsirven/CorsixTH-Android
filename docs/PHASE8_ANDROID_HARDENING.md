# Phase 8 — Android 14/15/16 hardening

Last updated: 2026-10-07

Status: **DONE** (code/build hardening complete; device matrix below)

**Branch:** `feature/corsixth-0.70-modernization`

## Goal

Remove compatibility debt for modern Android while keeping the SDL2 / arm64 / CorsixTH 0.70.1 gameplay path.

## SDK targets

| Item | Value |
|---|---|
| `minSdk` | 27 |
| `compileSdk` | **36** (Android 16) |
| `targetSdk` | **36** |
| ABI | `arm64-v8a` only |
| NDK | 27.0.12077973 + `APP_SUPPORT_FLEXIBLE_PAGE_SIZES` |

## Audit results and changes

| Area | Finding | Action |
|---|---|---|
| Scoped storage | Already SAF (`OpenDocumentTree` / `OpenDocument`); engine/TH under `noBackupFilesDir` | Kept; no `READ/WRITE_EXTERNAL_STORAGE` |
| Exported components | Only launcher `GameActivity` exported | Confirmed; others `exported=false` |
| Package visibility | `ACTION_VIEW` https privacy policy | Added `<queries>` for http/https `VIEW` |
| Notifications | No app-owned notification channels; extract notifications disabled | No `POST_NOTIFICATIONS` required |
| Pending intents (SDL HID) | USB permission already uses `FLAG_MUTABLE` + package | Left as-is |
| Dynamic receivers (SDL HID) | `registerReceiver` without export flag unsafe on API 33+ | `RECEIVER_NOT_EXPORTED` + typed `getParcelableExtra` |
| File / content URI | No `file://` sharing / FileProvider | N/A |
| Edge-to-edge | Enforced for target 35+; opt-out removed on target 36 | Game: cutout short-edges; Setup/Settings/Save: `setDecorFitsSystemWindows(true)` |
| Backup | Saves under `filesDir`; TH/engine under no-backup | `fullBackupContent` + `dataExtractionRules` |
| 16 KB pages | Required for Android 15+ devices | NDK flexible page sizes + `android:pageSizeCompat="enabled"` safety net |
| Deprecated APIs | Play Games still uses `startActivityForResult` | Documented debt (Phase 9 community cleanup) |
| ARM64-only | Already enforced | Unchanged |

## Exit criteria

| Criterion | Status |
|---|---|
| Functional on Android 14 | Manual — same APK path as Phase 7 smoke (target 36 runs on 14) |
| Functional on Android 15 | Manual — edge-to-edge + cutout path exercised in code |
| Functional on Android 16 | Manual when device/emulator available; compile/target 36 ready |
| Known limitations documented | See below |

## Known device-specific limitations

1. **16 KB page-size dialog** — `pageSizeCompat=enabled` suppresses the Android 16 compatibility warning if any packaged ELF is still 4 KB-aligned. Prefer verifying `libappmain.so` / SDL libs with Google’s alignment script when a 16 KB emulator is available.
2. **Steam Controller BLE** — SDL HID Bluetooth path still needs `BLUETOOTH_CONNECT` on Android 12+ if that path is used; not enabled in this community build (USB gamepads unaffected).
3. **Large-screen orientation** — Android 16 may ignore orientation / aspect constraints on ≥600dp displays; game remains landscape-oriented via SDL / config.
4. **Play Games UI** — `startActivityForResult` for achievements is deprecated; community builds skip Play Games entirely (`COMMUNITY_BUILD`).
5. **MIDI / SDL3** — still deferred (Phase 11 / out of scope).

## Validation

| Check | Result |
|---|---|
| `assembleDebug` (target/compile 36) | **SUCCESS** (`build-phase8.log`) |
| Manifest merge (exported / queries / backup) | applied |
| No broad storage permission | OK |

## Deferred to later phases

- Phase 9: strip Firebase / Play Games from community flavor
- Phase 10: release checksums / signing docs
- Full interactive re-test matrix on physical Android 14 / 15 / 16 devices
