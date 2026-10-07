# Phase 4 — CorsixTH 0.70.1 core bootstrap

Last updated: 2026-10-07

Status: **DONE** (native `libappmain` builds; GameActivity stays resumed on device)

## Goal

Replace the historical Android CorsixTH core (~0.67) with **v0.70.1** sources while keeping the Android Java/Kotlin shell, SDL2 glue, and touch/settings hooks.

## Submodule / branch

| Item | Value |
|---|---|
| Parent branch | `feature/corsixth-0.70-modernization` |
| Submodule branch | `feature/android-phase4-0.70.1` |
| Upstream tag | `v0.70.1` (`56bd5d00`) |
| Phase 3 tip (pre-import) | `473312fa` |

No push performed (local only).

## What was imported / restored

- Imported 0.70.1 `CorsixTH/Src`, `Lua`, `Bitmap`, campaigns/levels/assets.
- Restored Android Bitmap assets (`android.*`) and `Android/` glue (`androidhooks`, `androidevents`, logger).
- Kept Android-only dialogs (`android_menu*`, `android_play_menu`).

## Native patches for Android bootstrap

| Area | Change |
|---|---|
| `Android.mk` | 0.70.1 sources + stubs (`th_strings`, `midi_player`/`th_lua_midi` compile with MIDI device off) |
| `config.h` | Android defines; `WITH_MIDI_DEVICE` undefined |
| `appmain.cpp` | `lua_main` → `lua_init` (0.70 rename) |
| `androidevents.*` | Map Android events to 0.70 dispatch names (`touch*`, `configupdate`) |
| `sdl_core.cpp` | Android default event case → `androidPushEventArgs` |
| `th_lua*` | Register `TH.android` on `__ANDROID__` |
| `th_gfx_sdl.cpp` | Android PNG write via **lodepng** (no libpng) |

## Lua Android merges

Ported from Phase 3 Android fork into 0.70.1 Lua:

- Touch → mouse synthesis (`ui.lua`)
- `App:onTouch*` / `App:updateConfig` / `reportError` hook (`app.lua`)
- Android menu button + scroll modes (`game_ui.lua`)
- Main menu Options → Android settings (`main_menu.lua`)
- `originalWidth/Height`, default `scroll_mode`

## Runtime fix discovered on device

`App:fixConfig` crashed on Android:

```text
app.lua:fixConfig — attempt to index a nil value (local 'value')
```

Cause: `os.getenv("USER")` / `USERNAME` are unset on Android; code called `:match` on `nil`.

Guard added: treat missing env as `""`, then fall back to `"PLAYER"`.

## Validation

| Check | Result |
|---|---|
| `assembleDebug` | **SUCCESS** |
| ABI | arm64-v8a only (~43.9 MiB APK) |
| Install | `adb install -r` OK |
| Launch | `GameActivity` **Resumed** and stays up |
| Screencap | Device returns black frame (likely secure/SDL surface); activity focus confirms UI alive |
| Crashlytics | Pre-fix: `NativeLuaException` in `fixConfig`; post-fix: no immediate new event |

## Explicitly deferred

- MIDI device / FluidSynth path
- SDL3
- libpng instead of lodepng
- Full Phase 6 gameplay polish / Phase 7 save lifecycle
- Push to remote / CorsixTH fork publish

## Next

Phase 5 — data packaging/import hardening, then Phase 6 input/UI/audio essentials once gameplay is exercised manually on device.
