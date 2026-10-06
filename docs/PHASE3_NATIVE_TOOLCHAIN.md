# Phase 3 — Native toolchain readiness for CorsixTH 0.70.1

Last updated: 2026-10-06

Status: **toolchain prepared on historical core; submodule not replaced**

## Goal

Make the Android `ndk-build` environment explicitly compatible with CorsixTH 0.70.x requirements **before** swapping the engine sources (Phase 4).

## Verified toolchain

| Item | Value |
|---|---|
| NDK | 27.0.12077973 (`r27`) |
| STL | `c++_static` |
| C++ standard | **C++17** via `APP_CPPFLAGS` + engine `LOCAL_CPPFLAGS` |
| `APP_PLATFORM` | `android-27` |
| Primary ABI | **`arm64-v8a`** (`APP_ABI`) |
| Native entry | `jni/Android.mk` → subdir makefiles |
| Engine module | `jni/CorsixTH/Android.mk` → `libappmain.so` |
| Multimedia | **SDL2** retained (no SDL3) |

## Native dependency inventory

| Dependency | Location | Version / notes | 0.70.1 readiness |
|---|---|---|---|
| SDL2 | `jni/SDL` | **2.30.10** | Keep for first modernization cycle |
| SDL2_mixer | `jni/SDL_mixer` | **2.6.3** (WAV/OGG/MP3 via stb/dr_*; TiMidity on) | Keep; FluidSynth MIDI device path deferred |
| SDL2_gfx | `jni/SDL_gfx` | vendored static lib | Keep while Android UI needs it |
| Lua | `jni/LUA` | **5.4.6** | Compatible with 0.70.1 direction |
| LuaFileSystem | `jni/LFS` | vendored; force-preloaded on Android | Keep (`CORSIX_TH_LINK_LUA_MODULES`) |
| LPeg | `jni/LPEG` | vendored; force-preloaded | Keep |
| FreeType | `jni/freetype2` | **2.13.0** | Keep |
| FFmpeg | `jni/ffmpeg/<ABI>/…` | **prebuilt static** libs per ABI | arm64-v8a present; API drift risk in Phase 4 |
| lodepng | `jni/lodepng` | **20150418** | Temporary screenshot path; see PNG decision |
| zlib | NDK sysroot (`-lz`) | system | OK |
| AGG | referenced historically | **missing from tree** | Dead include removed in Phase 3 |

## PNG strategy decision

| Option | Pros | Cons |
|---|---|---|
| Keep **lodepng** (current Android fork) | Already linked; known working on current core | Diverges from upstream 0.70.1 libpng writer |
| Adopt **libpng** (upstream 0.70.1) | Aligns with desktop CorsixTH | Needs new Android.mk / headers / ABI builds |

**Decision for Phase 3 → 4:** keep **lodepng** for the first 0.70.1 compile attempt if upstream screenshot code can be adapted or temporarily stubbed; prefer converging to **libpng** once `libappmain.so` links successfully. Do not block Phase 4 on a full libpng port.

## `config.h` Android adaptations (Phase 3)

Hand-maintained `jni/CorsixTH/config.h` now:

- reports `CORSIX_TH_OS "android"` and `CORSIX_TH_ARCH "arm64-v8a"`
- defines `CORSIX_TH_LINK_LUA_MODULES` (matches Android preload of lfs/lpeg)
- leaves `WITH_MIDI_DEVICE` / `WITH_UPDATE_CHECK` / `WITH_TRACY` undefined
- includes Tracy macro stubs from upstream `config.h.in`

Phase 4 must revisit this file against 0.70.1 `config.h.in` (several desktop macros were removed/renamed upstream).

## `Android.mk` readiness notes for Phase 4

When the 0.70.1 sources are introduced, expect to add at least:

```text
CorsixTH/Src/th_strings.cpp
```

Deferred unless required for link:

```text
CorsixTH/Src/midi_player.cpp
CorsixTH/Src/th_lua_midi.cpp
```

Also expect:

- `#include <filesystem>` / `string_view` usage in 0.70.1 `main.cpp` → needs NDK libc++ (already `c++_static` + C++17)
- include path cleanup already done (removed missing `AGG`, added `Android/`, `lodepng`, mixer `include/`)
- Android user-event base offset updated to **5** so it sits after `SDL_USEREVENT_SOUND_OVER`

## Known blockers / risks before Phase 4

1. **Source tree swap** still required — toolchain ready ≠ 0.70.1 compiling.
2. **FFmpeg prebuilt ABI/API** may not match what 0.70.1 `th_movie.cpp` expects.
3. **PNG / libpng** divergence must be resolved during first compile errors, not before.
4. **MIDI / FluidSynth / RtMidi Android** intentionally out of scope for first boot.
5. **Lua script delta** (new 0.70 dialogs, removed Android-only menus) is a Phase 4/5 concern, not solved by toolchain flags.
6. Secondary ABIs (`armeabi-v7a`, `x86`, `x86_64`) are no longer built by default; prebuilts remain for a later restore.

## Explicit non-goals completed / avoided

- No CorsixTH submodule replacement in Phase 3
- No SDL3
- No CMake migration of the Android app
- No GitHub Actions

## Validation (verified 2026-10-06)

```powershell
$env:JAVA_HOME = "...\tools\jdk-17.0.14+7"
$env:ANDROID_SDK_ROOT = "E:\000-lastBeacon_ia\android-sdk"
.\gradlew.bat clean assembleDebug
```

| Check | Result |
|---|---|
| `assembleDebug` | **BUILD SUCCESSFUL** (~9m 28s after native clean) |
| NDK tasks | only `configureNdkBuildDebug[arm64-v8a]` / `buildNdkBuildDebug[arm64-v8a]` |
| APK ABI filter | `android.defaultConfig.ndk.abiFilters "arm64-v8a"` + `APP_ABI := arm64-v8a` |
| APK native libs | **arm64-v8a only** (`libappmain`, `libSDL2`, `libSDL2_mixer`, `libLUA`, …) |
| APK size | **~43.5 MiB** (was ~76 MiB multi-ABI) |

AGP note: setting `APP_ABI` alone can leave stale multi-ABI `.so` files packaged; `ndk.abiFilters` is required to enforce the milestone ABI in the APK.
