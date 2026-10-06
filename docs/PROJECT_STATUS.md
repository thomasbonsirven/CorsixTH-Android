# CorsixTH Android Modernization â€” Project Status

Last updated: 2026-10-06

## Current branch

`feature/corsixth-0.70-modernization`

Base branch:

`dev`

`dev` is intentionally left untouched while modernization work is validated.

## Current phase

**Phase 1 â€” Reproducible local build of the inherited port**

Status: **PARTIAL â€” assembleDebug verified on Windows; device install not yet validated**

Previous:

**Phase 0 â€” Baseline and repository control** â€” documentation established; local toolchain inventory completed via the Phase 1 build attempt.

## GitHub Actions status

**DO NOT RUN without explicit repository-owner approval.**

The inherited workflows are retained as-is for reference only.

Development validation is currently local-first.

Self-hosted runner `GAMING` exists but must not be used without explicit approval.

## Modernization target

First target core:

`CorsixTH 0.70.1`

First target architecture:

`arm64-v8a`

Primary Android targets:

- Android 16
- Android 15
- Android 14

SDL target for the first modernization cycle:

`SDL2`

SDL3 is deferred to a future independent phase.

## Verified Windows baseline (2026-10-06)

The following values were **verified by executing** a local Windows build, not only by reading source.

| Item | Verified value |
|---|---|
| Host OS | Windows 11 Professionnel (`10.0.26200`) |
| Branch | `feature/corsixth-0.70-modernization` |
| HEAD at build time | `dba08e90d336f246b399e42c63e7fed9d3fab89f` (+ local Phase-1 build fixes) |
| CorsixTH submodule | `fa1ca3e2d9bbe3721351040bc98812bdf1f0bac0` (`alanwoolley/CorsixTH`, detached at Android fork revision) |
| JDK | Eclipse Temurin / OpenJDK **17.0.14+7** (portable local install) |
| Gradle wrapper | **8.9** |
| Android Gradle Plugin | **8.7.3** |
| Kotlin | **2.0.21** |
| Android SDK root used | `E:\000-lastBeacon_ia\android-sdk` |
| Platform | `platforms;android-35` |
| Build-Tools | `35.0.1` |
| Platform-Tools / ADB | `37.0.1` |
| NDK used | **27.0.12077973** (`r27`) |
| NDK path | `E:\000-lastBeacon_ia\android-sdk\ndk\27.0.12077973` |
| Native entry | `jni/Android.mk` via `ndkBuild` |
| STL / platform | `c++_static` / `android-27` |
| Command | `.\gradlew.bat assembleDebug --stacktrace` |
| Result | **BUILD SUCCESSFUL in 8m 43s** |
| APK path | `build/outputs/apk/debug/CorsixTH-Android-debug.apk` |
| APK size | **76.47 MiB** (80â€¯179â€¯730 bytes) |
| APK package | `uk.co.armedpineapple.cth` |
| APK versionName | `1.0.0-SNAPSHOT` |
| ABIs in APK | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |
| Native libs observed | `libappmain.so`, `libSDL2.so`, `libSDL2_mixer.so`, `libLUA.so`, `libinnoextract.so`, Crashlytics NDK libs |
| Device test | **Not performed** â€” no authorized `adb devices` attached |

### Environment notes

- At session start, `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, and `java`/`adb` on PATH were unset.
- A partial Android SDK already existed at `E:\000-lastBeacon_ia\android-sdk` (platform 35 + platform-tools).
- Missing components installed with `sdkmanager`: `build-tools;35.0.1`, `ndk;27.0.12077973`.
- Portable JDK extracted under `E:\00-Dev_After_Renamer\101-CrosixTH_Android\tools\jdk-17.0.14+7` (outside the Git repository).
- Local `local.properties` points to the SDK root and is gitignored.

### Build blockers encountered and fixes

1. **Missing `google-services.json`**
   - First root failure: `:processDebugGoogleServices`
   - Minimal reversible fix: committed community stub at `distribution/community/google-services.community.json` and copy-into-place logic in `build.gradle` when the private file is absent.
   - Private/real `google-services.json` remains gitignored and is preferred when present.

2. **NDK selection not pinned in Gradle**
   - Successful baseline used NDK **27.0.12077973**.
   - `android.ndkVersion "27.0.12077973"` was added so future builds do not depend on deprecated `ndk.dir`.

### Important warnings observed (non-fatal)

- Deprecated `ASensorManager_getInstance` in SDL Android sensor code.
- `th_lua.cpp` non-void function missing return warning.
- Firebase Performance ASM instrumentation could not resolve some AndroidX/Window classes.
- Room `annotationProcessor` vs `kapt` configuration warning for `room-compiler`.

## Inherited repository configuration (still accurate)

| Component | Current inherited value |
|---|---|
| Default/base branch | `dev` |
| Android Gradle Plugin | `8.7.3` |
| Kotlin | `2.0.21` |
| KSP | `2.0.21-1.0.28` |
| Gradle wrapper | `8.9` |
| `minSdkVersion` | `27` |
| `targetSdkVersion` | `35` |
| `compileSdk` | `35` |
| Native build entry | `jni/Android.mk` via `ndkBuild` |
| STL | `c++_static` |
| NDK platform declaration | `android-27` |
| CorsixTH submodule URL | `https://github.com/alanwoolley/CorsixTH.git` |
| CorsixTH submodule branch hint | `Android` |
| CorsixTH submodule gitlink | `fa1ca3e2d9bbe3721351040bc98812bdf1f0bac0` |
| Firebase/Google plugins | Present in inherited build; community stub allows local compile |
| Google Play Games | Present in inherited build |

## Known architectural concern

The Android app does not directly track upstream CorsixTH. It references the dedicated historical Android fork/branch:

`alanwoolley/CorsixTH : Android`

Therefore the migration to CorsixTH 0.70.1 must begin with a delta analysis rather than a direct submodule replacement.

## Phase tracking

| Phase | Description | State |
|---|---|---|
| 0 | Baseline/repository control | DONE (docs + toolchain inventory via Phase 1) |
| 1 | Reproducible inherited local build | PARTIAL (`assembleDebug` OK; device install pending) |
| 2 | Android fork vs 0.70.1 delta analysis | NOT STARTED |
| 3 | C++17/native toolchain modernization | NOT STARTED |
| 4 | CorsixTH 0.70.1 bootstrap | NOT STARTED |
| 5 | Data packaging/import | NOT STARTED |
| 6 | Input/UI/audio/gameplay essentials | NOT STARTED |
| 7 | Save/load/lifecycle reliability | NOT STARTED |
| 8 | Android 14/15/16 hardening | NOT STARTED |
| 9 | Community build cleanup | NOT STARTED |
| 10 | Release engineering | NOT STARTED |
| 11 | SDL3 migration | FUTURE / OUT OF SCOPE |

## Phase 1 remaining checklist

- [x] Initialize historical CorsixTH submodule at recorded revision.
- [x] Install/pin JDK 17 for AGP 8.7.3.
- [x] Use repository Gradle wrapper 8.9.
- [x] Install Android SDK Platform 35, Build-Tools 35.0.1, NDK 27.0.12077973.
- [x] Resolve missing `google-services.json` without committing secrets.
- [x] `assembleDebug` succeeds and produces an APK.
- [x] Record APK path, size and ABIs.
- [x] Install APK on at least one ARM64 Android device via ADB.
- [x] Confirm application reaches setup/first screen without immediate crash.
- [ ] Optional: document a one-command local env setup script (later, if useful).


## Runtime crash fix (2026-10-06, verified on device)

Device: nubia NX789J, Android 16

Verified root cause via DropBox `data_app_crash`:

```
java.lang.IllegalArgumentException: Please set a valid API key.
  at com.google.firebase.installations.FirebaseInstallations.preConditionChecks
  at com.google.firebase.perf.transport.TransportManager...
```

Cause: community `google-services` stub + Firebase Performance.

Fix verified:

- Firebase Performance plugin/dependency disabled when community stub is detected
- Play Games SDK / auto sign-in skipped for `BuildConfig.COMMUNITY_BUILD`
- After reinstall: process stays alive on `SetupActivity` (no Theme Hospital data yet)
- No new DropBox crash after the fixed APK install

## Immediate next task

**Do not migrate CorsixTH yet.**

Next engineering task: install the debug APK on an ARM64 Android device/emulator and confirm the first-launch/setup screen, then close Phase 1.

No workflow execution is required for Phase 1.

## Documentation control

Mandatory documents:

- `DEVELOPMENT_POLICY.md`
- `docs/DEVELOPMENT_PHASES.md`
- `docs/AI_AGENT_RULES.md`
- `docs/PROJECT_STATUS.md`

Any AI/developer continuing this work must read these files before implementation.

