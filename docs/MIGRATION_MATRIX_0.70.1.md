# Migration matrix — Android CorsixTH fork → CorsixTH 0.70.1

Last updated: 2026-10-06

Status: **Phase 2 analysis (no core replacement performed)**

## Compared revisions

| Side | Repository | Revision | Notes |
|---|---|---|---|
| Android fork (current submodule) | `alanwoolley/CorsixTH` | `fa1ca3e2d9bbe3721351040bc98812bdf1f0bac0` | Detached at recorded gitlink; AndroidDev lineage |
| Upstream target | `CorsixTH/CorsixTH` | `v0.70.1` → `56bd5d00f76331c7f76d7b696726a7926303ca0c` | First modernization target |
| Approximate merge-base | shared history | `fc38c4f4` (`Bump version for 0.67 release`) | Android fork last synced near **0.67** era |

Approximate divergence from merge-base:

- Android-only commits since base: **~157**
- Upstream commits to reach 0.70.1: **~1235**

**Conclusion:** blind submodule replacement is unsafe. Android-specific glue must be forward-ported onto 0.70.1 (or reapplied as a patch set) after updating shared core sources.

---

## High-level deltas

### Native `CorsixTH/Src`

| Only in Android fork | Only in 0.70.1 |
|---|---|
| *(no unique Src filenames beyond Android glue outside Src)* | `cpmik_table.h`, `midi_player.{cpp,h}`, `sdl_core.h`, `th_lua_midi.cpp`, `th_lua_ui.h`, `th_strings.cpp` |

Android glue lives primarily in:

```text
jni/CorsixTH/Android/
  androidevents.{cpp,h}
  androidhooks.{cpp,h}
  logger.cpp
```

plus `jni/CorsixTH/Android.mk` and `CorsixTH/appmain.cpp`.

### Lua

| Only in Android fork | Only in 0.70.1 (examples) |
|---|---|
| `dialogs/android_menu_button.lua` | `earthquake.lua`, `endconditions.lua` |
| `dialogs/android_play_menu.lua` | `dialogs/subtitles.lua` |
| `dialogs/resizables/android_menu.lua` | `dialogs/resizables/adviser_history.lua` |
| `run_debugger.lua` | `dialogs/resizables/machine_menu.lua`, `sound_setting.lua`, … |
| | `languages/japanese.lua`, `languages/ukrainian.lua` |

Android Lua still calls `TH.android.*` / `TH.android_events.*` and handles `touchdown` / `touchup` / `touchmove` / `configupdate`.

### Toolchain / build

| Item | Android fork today | 0.70.1 desktop |
|---|---|---|
| Native build | `ndk-build` + many `Android.mk` | CMake + vcpkg |
| C++ standard | **Already `-std=c++17`** in `jni/CorsixTH/Android.mk` | C++17 |
| Screenshots | `lodepng` path in Android fork | libpng PNG writer in upstream |
| MIDI | not in Android `Android.mk` source list | `midi_player` + `th_lua_midi` |
| Linked Lua modules | force-preload `lfs` / `lpeg` in `main.cpp` | optional / different packaging |

---

## Migration matrix

| Android patch / behavior | Still required? | 0.70.1 equivalent | Migration strategy | Status |
|---|---|---|---|---|
| `Android/androidhooks.cpp` JNI ↔ Java (`GameActivity`, config, save DB, errors, Play Games entry points) | **Yes** (core Android integration) | None upstream | Keep as Android-only module; re-bind against 0.70.1 Lua registration APIs | TO PORT |
| `Android/androidevents.cpp` touch + custom SDL user events (`load`/`save`/`configupdate`/…) | **Yes** | Upstream has more base `SDL_USEREVENT_*` (incl. `SOUND_OVER`) | Rebase event IDs carefully; update `CTH_USEREVENT_COUNT` (currently 4, may collide with `SOUND_OVER` at +4) | TO PORT / AUDIT |
| `sdl_core.cpp` call into `handleAndroidEvents` | **Yes** | Upstream event loop evolved (music async / helpers) | Re-apply Android branch inside 0.70.1 `sdl_core.cpp` rather than keeping old file wholesale | TO PORT |
| `th_lua.cpp` register `android` / `android_events` metatables | **Yes** | Upstream registration list changed (midi, strings, etc.) | Port `#ifdef __ANDROID__` registration into 0.70.1 `th_lua.cpp` + `th_lua_internal.h` | TO PORT |
| `th_lua_internal.h` `lua_metatable::android` / `android_events` | **Yes** | N/A | Re-add enum entries in 0.70.1 header | TO PORT |
| Force `lfs` / `lpeg` preload in `main.cpp` | **Yes** for current ndk-build packaging | Desktop uses different module discovery | Keep Android preload until packaging strategy changes | KEEP (Android) |
| `appmain.cpp` Android entry / cleanup around `lua_main` | **Yes** | Desktop `main.cpp` path differs | Keep Android entry; ensure symbols match 0.70.1 `main.h`/`bootstrap` | TO PORT |
| `logger.cpp` Android logging hooks | Likely yes | N/A | Retain unless replaced by SDL/logcat strategy | TO PORT |
| Touch synthesis / scroll mode / edge-scroll Android commits | **Yes** for playability | Upstream mouse/touch differs | Port Lua `app.lua` touch handlers + related UI; validate against 0.70.1 UI scaling | TO PORT |
| Android menus: settings/load/save/Play Games buttons | Partially | Upstream menus redesigned (0.70 UI scaling, new dialogs) | Re-integrate Android menu hooks into 0.70.1 main menu; Play Games optional for community builds | TO PORT |
| Achievement / economy event hooks (`android_events`) | Optional for community; useful for Play flavor | N/A | Port behind community/Play build flags | DEFER / OPTIONAL |
| PNG screenshots via `lodepng` (`th_gfx_sdl.cpp`) | Prefer converging | Upstream now uses libpng PNG screenshots | Prefer adopting upstream PNG path if Android can link libpng; else keep lodepng temporarily | EVALUATE |
| Window resize / display aspect Android tweak | Likely yes | Upstream window/WM changes | Re-test on 0.70.1; port only if still needed | AUDIT |
| Movie / FFmpeg integration | **Yes** | Upstream movie stack evolved | Rebuild against 0.70.1 `th_movie.*` with existing Android FFmpeg mk | TO PORT |
| MIDI player / FluidSynth stack | Not required for first Android milestone | New in 0.70.x | **Defer** for first bootable Android 0.70.1; add later if needed | DEFER |
| Russian edition / UI 2x–3x scaling / accessibility (0.70 features) | Desired later | Present upstream | Arrive “for free” once core+Lua updated; then validate on phones/tablets | AFTER BOOT |
| Android.mk source list | **Yes** until CMake migration | CMakeLists.txt | Extend source list for new 0.70.1 `.cpp` files (`th_strings.cpp`, maybe midi later) | TO UPDATE |
| C++17 flag | Already present in engine `Android.mk` | Required | Keep; do not regress to C++11 | DONE (engine mk) |
| SDL2 retention | **Yes** for this cycle | 0.70.1 still SDL2-era | Keep vendored `jni/SDL` / `SDL_mixer`; do **not** start SDL3 | KEEP |
| `config.h` hand-maintained Android copy | **Yes** short-term | Generated from `config.h.in` via CMake | Regenerate/adapt Android `config.h` for 0.70.1 feature macros | TO UPDATE |
| Google/Firebase/Play Games Java layer | Community: no; Play: optional | N/A | Already partially gated by `COMMUNITY_BUILD`; keep out of native migration critical path | SEPARATE TRACK |

---

## Recommended migration order (after Phase 2 close)

Do **not** replace the submodule in one step.

1. **Inventory freeze** — keep `fa1ca3e2` as known-good baseline (Phase 1 APK).
2. **Toolchain prep (Phase 3)** — ensure ndk-build source list + `config.h` can accept 0.70.1 sources; keep SDL2; decide PNG strategy (libpng vs lodepng).
3. **Core bootstrap (Phase 4)** — introduce 0.70.1 sources beside Android glue; fix compile/link until `libappmain.so` loads.
4. **Re-apply Android glue** — `androidhooks` / `androidevents` / `sdl_core` hook / Lua metatables / `appmain`.
5. **Lua merge** — bring 0.70.1 Lua, then re-apply Android-only dialogs and touch/config hooks.
6. **First launch** — reach Setup / main menu before chasing MIDI, achievements, or UI polish.

---

## Explicit non-goals for the next engineering step

- No SDL3.
- No blind `git submodule` switch to `v0.70.1`.
- No mass deletion of `Android/` glue.
- No GitHub Actions / runner usage.
- No Play Store / Firebase restoration requirement for community builds.

---

## Evidence commands used

```text
git -C jni/CorsixTH rev-parse HEAD
# fa1ca3e2...

git clone --depth 1 --branch v0.70.1 https://github.com/CorsixTH/CorsixTH.git compare/CorsixTH-0.70.1
# 56bd5d00... (v0.70.1)

git -C jni/CorsixTH merge-base HEAD 56bd5d00...
# fc38c4f4

git -C jni/CorsixTH rev-list --left-right --count HEAD...56bd5d00...
# 157  1235
```

Comparison checkout lives outside the Git repo at:

`E:\00-Dev_After_Renamer\101-CrosixTH_Android\compare\CorsixTH-0.70.1`
