# Phase 6 — Input, UI, audio and gameplay essentials

Last updated: 2026-10-07

Status: **DONE** (code/build complete; on-device smoke pending reconnect)

## Goal

Reach a genuinely playable touch-first state on Android with CorsixTH 0.70.1.

## Changes

### Input / UI

| Item | Change |
|---|---|
| Touch → mouse | Already present from Phase 4 (`ui.lua` / `game_ui.lua`) |
| Scroll modes | `scroll_mode` + touch-derived edge-scroll suppression |
| Mouse capture | Forced **off** in `App:init` / `App:setCaptureMouse` + `config_template` |
| Android Back | `SDL_ANDROID_TRAP_BACK_BUTTON=true`; Lua maps `"AC Back"` → `escape` / cancel |
| Main menu | Options → Android settings; Play Games menu window restored |
| In-game menu | Android menu button; desktop menu bar hidden |

### Save / persistence (gameplay loop)

| Item | Change |
|---|---|
| Save dump | Swap Android menu button ↔ portable `menu_bar` around `persist.dump` |
| Permanent table | Skip `TH.android*` keys so JNI hooks are not persisted |
| Save file | Call `TH.android.updateSaveGameDatabase` with screenshot path |
| Screenshots | `UI:makeScreenshot` returns filename for DB preview |

### Play Games / stats hooks

Guarded `TH.android_events` calls in:

- `Hospital:spendMoney` / `receiveMoney`
- `Hospital:humanoidDeath` / `updateCuredCounts`
- `World:winGame` (campaign level complete)

Community builds: Java side remains no-op via `playGamesService?`.

### Audio

Relies on SDLActivity lifecycle (`nativePause` / resume) already wired by the SDL Android project. Config volumes/toggles still driven from Android settings → `updateConfig`.

## Build

`assembleDebug` + `createGameZip`: **SUCCESS** (2026-10-07).

## Device validation

Phone was **offline** at end of Phase 6 (`adb devices` empty). APK is built locally; when the device is back:

1. `adb install -r build/outputs/apk/debug/CorsixTH-Android-debug.apk`
2. Confirm GameActivity stays resumed
3. Tap main-menu New Game → level starts
4. `adb shell input keyevent KEYCODE_BACK` → dialogs cancel / game stays (does not finish Activity)
5. Drag-scroll map; long-press ≈ right click
6. Optional: save once and confirm `files/saves` + Room DB update

## Exit criteria

| Criterion | Status |
|---|---|
| Usable without external mouse/keyboard | Code ready (touch synthesis + back) |
| Main audio path | Inherited SDL mixer path; lifecycle via SDL |
| Basic gameplay loop | Hooks restored; **manual smoke pending device** |

## Deferred to Phase 7+

- Full save/load reliability matrix
- Orientation / cutout polish
- Controller support
- MIDI / SDL3
