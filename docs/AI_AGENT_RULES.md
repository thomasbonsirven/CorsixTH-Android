# AI / Automated Development Agent Rules

This file defines mandatory constraints for any AI agent, coding assistant or automated tool working on the CorsixTH Android modernization branch.

## 1. Mandatory branch

Work only on:

`feature/corsixth-0.70-modernization`

or on a short-lived child branch explicitly created from it.

Do not modify `dev` or `master` unless the repository owner explicitly requests it.

## 2. GitHub Actions prohibition

Until explicit owner approval:

- do not trigger GitHub Actions;
- do not manually run workflows;
- do not rerun workflow jobs;
- do not modify workflow files for execution;
- do not merge into a branch whose push will trigger the inherited workflows;
- do not configure a GitHub-hosted or self-hosted runner;
- do not publish release artifacts automatically.

Local Windows build/test is the authoritative validation path.

## 3. Read before editing

Before changing code, inspect at minimum:

- `DEVELOPMENT_POLICY.md`
- `docs/DEVELOPMENT_PHASES.md`
- `docs/PROJECT_STATUS.md`
- `.gitmodules`
- `build.gradle`
- `gradle/wrapper/gradle-wrapper.properties`
- `jni/Application.mk`
- `jni/Android.mk`
- relevant native dependency `Android.mk` files

When changing the CorsixTH core, inspect the Android fork delta before replacing anything.

## 4. One phase at a time

Do not attempt multiple migration phases at once.

The active phase is recorded in `docs/PROJECT_STATUS.md`.

Do not start the next phase until the current phase exit criteria are satisfied or the owner explicitly overrides the gate.

## 5. No speculative mass rewrite

Forbidden without explicit owner approval:

- replacing the whole Android application architecture;
- migrating to Jetpack Compose;
- migrating SDL2 to SDL3 during the 0.70.1 modernization;
- replacing `ndk-build` with CMake only because it appears cleaner;
- deleting historical Android-specific code before understanding it;
- updating every dependency simultaneously;
- rewriting Java/Kotlin components unrelated to the active blocker;
- changing package/application identity;
- changing signing strategy;
- adding telemetry.

Prefer minimal, reversible changes.

## 6. CorsixTH target

First modernization target:

`CorsixTH 0.70.1`

Do not track upstream `master` for the first stable Android migration.

Reason: the project must stabilize on the final SDL2-era 0.70.x architecture before any later SDL3 migration.

## 7. C++ standard

The modernization target requires C++17.

When modifying native build settings:

- identify every place where C++11 is forced;
- change only the settings needed for C++17 compatibility;
- preserve exception/runtime requirements unless evidence shows otherwise;
- record compiler errors before patching source code around them.

Do not silence compiler errors with broad flags unless the reason is documented.

## 8. Android-first native patching

For each native compile error:

1. capture the exact error;
2. identify whether it comes from upstream 0.70.1, Android glue, dependency version or toolchain;
3. find the smallest valid fix;
4. prefer upstream-compatible logic;
5. build again;
6. document non-obvious Android-specific differences.

Do not apply large mechanical patches without intermediate builds.

## 9. Build evidence

After any build-affecting commit, record or report:

```text
Commit:
Phase:
Command:
JDK:
Gradle:
Android SDK:
NDK:
ABI:
Result:
First error if failed:
APK produced if successful:
Device test result if performed:
```

Never report a build as successful unless the build command actually completed successfully.

Never report the application as working unless it was installed/launched or the owner explicitly states the runtime test result.

## 10. Failure handling

When a build fails:

- stop at the first meaningful root-cause error;
- do not patch dozens of downstream errors independently;
- determine whether the later errors are cascading failures;
- preserve logs;
- make one focused fix;
- rebuild.

## 11. Dependency changes

Any dependency update must answer:

- Why is this update necessary now?
- What previous version/source was used?
- Does it support `arm64-v8a`?
- Does it alter API/ABI behavior?
- Does it alter licensing?
- Does it require an Android build-system change?

If not required by the active phase, defer it.

## 12. Proprietary assets

Never add Theme Hospital proprietary data to Git.

Never add:

- original game data;
- GOG installer;
- copied CD files;
- copyrighted game audio/video/assets.

Tests should use local user-owned data outside the repository.

## 13. Secrets and signing

Never commit or print:

- GitHub tokens;
- passwords;
- Android keystores;
- keystore passwords;
- Firebase service credentials;
- Google service-account credentials;
- private API keys.

Use local environment variables/files excluded from Git when needed.

## 14. Firebase/Google cleanup timing

Do not remove Firebase/Google dependencies during baseline reproduction or initial core migration unless they directly prevent the build.

First establish a working baseline and 0.70.1 bootstrap.

Then isolate/remove unnecessary cloud dependencies in the dedicated community-build phase.

This preserves regression traceability.

## 15. Generated files

Do not commit generated binaries or local build outputs unless explicitly requested.

Typical files that should remain untracked include:

- APK/AAB outputs;
- Gradle caches;
- `.gradle/`;
- local SDK paths;
- native object files;
- local signing material;
- imported Theme Hospital game data.

## 16. Commit quality

Each commit must be:

- build-oriented or logically atomic;
- understandable from its message;
- reversible;
- free of unrelated formatting churn;
- free of generated/proprietary files.

Preferred examples:

```text
build: pin local NDK version
native: enable C++17 for Android core
core: forward-port Android bootstrap to CorsixTH 0.70.1
storage: adapt game data import to scoped storage
fix: restore SDL audio resume after activity pause
```

Avoid messages such as:

```text
fix stuff
update
working changes
AI changes
misc
```

## 17. Documentation updates

When a phase changes state, update `docs/PROJECT_STATUS.md`.

When a new non-obvious workaround is introduced, document it in the repository in the same change set or immediately after it.

## 18. Definition of trustworthy output

An AI agent must clearly distinguish:

- **verified**: observed in repository/build/device output;
- **inferred**: strongly suggested by code or documentation;
- **proposed**: a recommended future change;
- **untested**: implemented but not validated.

Never present inferred or untested behavior as verified.

## 19. Stop conditions requiring owner decision

Stop and request owner direction before:

- triggering GitHub Actions;
- merging into `dev`;
- publishing anything;
- changing application/package identity;
- deleting compatibility code whose purpose is unclear;
- replacing the native build system;
- starting SDL3 migration;
- introducing telemetry;
- introducing a major dependency with licensing implications;
- committing a workaround that materially diverges from upstream CorsixTH behavior.

## 20. Primary engineering principle

The goal is not to modernize everything at once.

The goal is to move from a known Android port to a **working, reproducible, maintainable CorsixTH 0.70.1 Android build** through small, testable steps.
