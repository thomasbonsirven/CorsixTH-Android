# Phase 7 — Save/load and lifecycle reliability

**Status:** DONE (2026-10-07) — accepted by user after device smoke  
**Branch:** `feature/corsixth-0.70-modernization`

## Launch blocker fixed (pre-requisite)

After GOG Theme Hospital import, `GameActivity` reached splash then exited immediately.

**Root cause:** Android `sdl_core` uses a C++ `mainloop(L)` dispatcher, but `appmain.cpp` never called it after a successful `TheApp:init()`. Upstream relies on `return TheApp:run()` → `SDL.mainloop(co)`; that path was removed without wiring the C++ loop.

**Fix:** call `mainloop(L)` after successful `lua_pcall` in `CorsixTH/appmain.cpp`.

Related launch hardening in the same session:

- `initMusicDir` hardened for Android (`cwd` often `/`)
- Android intro/demo movies disabled (FFmpeg/GL abort path)
- `android.pal` preloaded + Android dialogs use `_loadPalette` (0.70.1 API)
- `jniLibs.useLegacyPackaging = true` so SDL can `dlopen` extracted `.so`

## Device smoke (post-fix)

| Check | Result |
|---|---|
| Setup menu without TH data | OK |
| Launch with GOG install | OK — main menu `v0.70.1` stays resumed |
| Home → resume task | OK |
| User acceptance | OK (Phase 7 signed off) |

## Exit criteria

- Save/load works after process restart — accepted via manual play  
- Normal Android lifecycle does not cause routine crashes or silent data loss — OK on smoke  

