# CorsixTH Android Modernization — Development Policy

> Scope: modernization of the Android port toward CorsixTH 0.70.x while preserving a stable, reviewable, reproducible development process.

## 1. Project objective

The modernization branch exists to produce a maintainable Android port of CorsixTH based on the current SDL2-era CorsixTH 0.70.x codebase, with modern Android/ARM64 support and a reproducible local build process.

The first target is **CorsixTH 0.70.1**. SDL3 migration is explicitly out of scope for the first modernization cycle.

Primary target:

- Android 14 / 15 / 16
- `arm64-v8a` first
- local reproducible build on Windows
- Theme Hospital original data imported by the user
- no proprietary Theme Hospital assets committed or redistributed

## 2. Branch policy

### Protected baseline

`dev` is the fork baseline and must remain untouched until the modernization branch reaches an explicit integration gate.

### Main modernization branch

Development takes place on:

`feature/corsixth-0.70-modernization`

Optional short-lived branches may be created from it for risky or isolated work:

- `phase/01-local-baseline`
- `phase/02-core-0.70.1`
- `phase/03-native-toolchain`
- `fix/<short-description>`
- `experiment/<short-description>`

### Merge rule

Nothing is merged into `dev` without explicit owner approval.

No force-push to `dev`.

## 3. GitHub Actions policy

**GitHub Actions MUST NOT be triggered, modified, enabled, rerun, or relied upon without explicit owner approval.**

Until approval is given:

- do not push changes directly to `dev` or `master`;
- do not edit `.github/workflows/*`;
- do not manually run any workflow;
- do not rerun failed jobs;
- do not configure a self-hosted runner from GitHub;
- do not publish APK/AAB artifacts through GitHub Actions;
- do not publish to Google Play.

The current development source of truth is the **local Windows build/test environment**.

If a future GitHub Actions workflow is prepared, it must remain unexecuted until explicit approval.

## 4. Change isolation

Each commit should solve one technical problem or one tightly related group of problems.

Preferred commit prefixes:

- `docs:` documentation only
- `build:` Gradle / NDK / toolchain / dependency changes
- `core:` CorsixTH core integration
- `android:` Java/Kotlin/Android platform integration
- `native:` C/C++/JNI/NDK changes
- `input:` touch/mouse/controller changes
- `audio:` SDL_mixer/audio changes
- `storage:` Android file/import/storage changes
- `fix:` bug fix
- `test:` tests or validation tooling
- `refactor:` behavior-preserving restructuring
- `ci:` CI definition only — requires explicit owner approval before use

Avoid commits mixing unrelated Gradle, C++, UI and gameplay changes.

## 5. Build-first rule

Before changing the CorsixTH core, the existing fork must compile locally from a clean checkout.

A phase is not considered complete because the source "looks correct". It must satisfy its exit criteria in `docs/DEVELOPMENT_PHASES.md`.

Every build-affecting change must record:

- command executed;
- Java/JDK version;
- Gradle version;
- Android SDK version;
- Android NDK version;
- ABI;
- build result;
- relevant warnings/errors.

## 6. Reproducibility

Do not depend on undocumented machine state.

Prefer explicit, version-pinned dependencies and toolchains.

Do not silently depend on:

- globally installed Gradle when the wrapper exists;
- unknown NDK versions;
- manually copied libraries not documented in the repository;
- untracked patches;
- IDE-only configuration;
- environment variables that are not documented.

If a manual step is unavoidable, document it before proceeding to the next phase.

## 7. Native code policy

CorsixTH 0.70.x requires C++17. The historical Android port currently contains Android-specific native build logic that must be migrated carefully rather than replaced wholesale.

Rules:

1. Preserve Android-specific behavior until its purpose is understood.
2. Do not mass-replace the old Android core with upstream files and then fix hundreds of errors blindly.
3. Compare the Android fork and upstream 0.70.1 before porting changes.
4. Keep patches as small and attributable as possible.
5. Prefer upstream-compatible code over Android-only forks when practical.
6. Keep SDL2 for the first modernization milestone.
7. Any SDL3 migration is a later independent project phase.

## 8. Dependency policy

For each native dependency, record:

- current version/source;
- why it is needed;
- ABI support;
- Android compatibility;
- whether it is vendored, built from source or fetched;
- license implications.

Important dependency families include:

- SDL2
- SDL2_mixer
- Lua
- LuaFileSystem
- LPeg
- FreeType
- FFmpeg
- zlib
- libpng
- AndroidX/Kotlin/Room dependencies

Do not update every dependency at once.

## 9. Community build policy

The modernization target is a clean community build.

Long-term goal:

- no advertisements;
- no analytics requirement;
- no mandatory Firebase runtime;
- no mandatory Google Play Games dependency;
- no Google Play dependency for core gameplay;
- offline gameplay after game data import;
- local save/load support.

Removal of Google/Firebase components must be done only after the baseline build is stable, so regressions remain attributable.

## 10. Proprietary data and legal boundary

Never commit, upload or redistribute proprietary Theme Hospital game data.

Do not commit:

- original game assets;
- GOG installer files;
- proprietary audio/video/data files;
- copyrighted ROM/CD contents;
- private signing keys or keystores.

The application may provide mechanisms to import user-owned data or supported demo data where legally appropriate.

## 11. Secrets

Never commit:

- keystores;
- passwords;
- tokens;
- service-account JSON;
- API keys;
- private certificates;
- personal environment configuration.

Local signing configuration must remain outside version control.

## 12. Testing rule

Each functional phase must eventually verify, when applicable:

- APK installs;
- application launches;
- setup wizard works;
- Theme Hospital data imports;
- main menu loads;
- a new game starts;
- sound/music works;
- touch input works;
- save works;
- force-stop/relaunch/load works;
- no obvious crash during a basic gameplay session.

A compile-only success is not equivalent to a functional success.

## 13. Android compatibility priority

Priority order:

1. `arm64-v8a`
2. Android 16
3. Android 15
4. Android 14
5. modern tablets and phones

Secondary support can later include:

- `armeabi-v7a`
- x86_64 emulator/debug

No legacy ABI should block the primary ARM64 milestone.

## 14. Regression policy

If a change breaks a previously validated milestone:

1. stop forward development;
2. identify the first bad commit;
3. revert or fix the regression;
4. revalidate the previous milestone;
5. only then continue.

Do not stack new features on top of a broken baseline.

## 15. Documentation requirement

Any non-obvious build workaround, Android-specific patch or dependency exception must be documented in the repository before the phase is closed.

Documentation is part of the implementation, not optional cleanup.

## 16. Owner approval gates

Explicit owner approval is required before:

- using GitHub Actions;
- changing workflow files with intent to run them;
- merging modernization work into `dev`;
- publishing a GitHub Release;
- publishing an APK/AAB publicly;
- publishing to Google Play;
- adding telemetry/analytics;
- changing package identity/signing strategy;
- starting SDL3 migration.

## 17. Definition of done for the first modernization release

The initial modernization release is done only when all of the following are true:

- CorsixTH 0.70.1-based core is integrated;
- project builds reproducibly on the documented Windows environment;
- ARM64 APK installs on modern Android;
- application launches without fatal error;
- original Theme Hospital data can be imported;
- main game loads;
- touch interaction is usable;
- audio is functional;
- save/load is functional;
- Android 14/15/16 behavior is validated or documented;
- no proprietary game data is shipped;
- known blockers are documented;
- owner approves integration/release.
