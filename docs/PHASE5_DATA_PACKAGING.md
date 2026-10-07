# Phase 5 — Data packaging and Theme Hospital import

Last updated: 2026-10-07

Status: **DONE** (engine packaging hardened; setup/import validated on device)

## Goal

Keep open-source CorsixTH engine data and proprietary Theme Hospital data reliably separated, packaged, and importable on modern Android (SAF, no broad storage permission).

## Separation model

| Data | Source | On-device location |
|---|---|---|
| CorsixTH engine (Lua/Bitmap/Levels/…) | APK asset `game.zip` | `no_backup/cth/` |
| Theme Hospital proprietary | User import (folder / GOG / demo) | `no_backup/themehospital/` |
| Saves / screenshots | Runtime | `files/saves`, `files/screenshots` |
| Timidity music lib | APK asset `timidity.zip` | `no_backup/timidity/` |

Manifest storage permissions: **none** beyond network (demo download). Folder/installer paths use SAF (`OpenDocumentTree` / `OpenDocument`).

## `game.zip` packaging

`createGameZip` includes:

- `CorsixTH.lua`, `Lua/**`, `Bitmap/**`, `Levels/**`, `Campaigns/**`, `Graphics/**`

Excludes Bitmap build helpers (`mk*.lua`, `lib_*.lua`, `readme.txt`, `example.spec`).

Validated markers present for 0.70.1 + Android UI:

- `Lua/earthquake.lua`, `Lua/endconditions.lua`
- `Lua/dialogs/android_menu_button.lua`, `Bitmap/android.dat`

## Engine install / upgrade

`FilesService`:

- `hasGameFiles()` — multi-marker check (not only `CorsixTH.lua`)
- `needsEngineDataUpgrade()` — markers + `Lua/api_version.lua` must equal **2717**
- writes `.cth_engine_stamp` (`api=2717`, `package=0.70.1`) after extract

`GameActivity` reinstalls from `game.zip` when first launch for version, upgrade needed, or `ALWAYS_UPGRADE` (debug).

## Theme Hospital import hardening

- Folder import resolves TH root at selected tree **or one nested directory**
- Rejects non-TH trees before copy (`ExtractResult.FAILURE`)
- After GOG/demo extract: `normalizeThemeHospitalInstall()` promotes nested roots
- Post-import requires `DATA/LANG-0.DAT` + `QDATA/AREA01V.DAT`
- SAF: `takePersistableUriPermission` on tree URI (read)

## Device validation (2026-10-07)

| Check | Result |
|---|---|
| `assembleDebug` | SUCCESS |
| Slim `game.zip` | 425 entries; helpers excluded |
| No TH → launch | **SetupActivity** resumed |
| TH restored → launch | **GameActivity** resumed |
| Engine stamp | `api=2717` / `package=0.70.1` |
| Markers on device | present |

## Exit criteria mapping

| Criterion | Status |
|---|---|
| Fresh install reaches setup wizard | OK (simulated by hiding `themehospital`) |
| User-owned TH data can be imported | OK (existing paths + validation) |
| CorsixTH finds data after restart | OK |
| No broad storage permission | OK (SAF only) |

## Deferred

- Full interactive GOG/demo re-import UI smoke (manual)
- Changing demo CDN URL / hosting
- Phase 6 gameplay/input polish
