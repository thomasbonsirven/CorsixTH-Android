# CorsixTH Android — NDK application settings (Phase 3 toolchain)
#
# Keep SDL2 / ndk-build for the 0.70.1 modernization cycle.
# Do not raise APP_PLATFORM here without a deliberate compatibility pass.

APP_STL := c++_static
APP_PLATFORM := android-27

# Primary modernization ABI. ffmpeg prebuilts for other ABIs remain on disk
# but are not built by default so local iteration matches the ARM64 milestone.
APP_ABI := arm64-v8a

# CorsixTH 0.70.x requires C++17. Also set on the engine module; keep both.
APP_CPPFLAGS := -std=c++17 -fexceptions

# Phase 8: build native shared libraries compatible with 16 KB page-size devices
# (Android 15+). Final LOAD segments are aligned via the NDK flexible-page support.
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true
