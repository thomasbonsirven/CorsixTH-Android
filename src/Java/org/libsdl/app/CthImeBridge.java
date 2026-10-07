package org.libsdl.app;

/**
 * Package-local bridge so GameActivity can drive SDL text input / keyboard flags
 * without relying on DummyEdit focus (often stolen by the GL SurfaceView).
 */
public final class CthImeBridge {
    private CthImeBridge() {
    }

    public static void setScreenKeyboardShown(boolean shown) {
        SDLActivity.mScreenKeyboardShown = shown;
    }

    public static boolean isScreenKeyboardShown() {
        return SDLActivity.mScreenKeyboardShown;
    }

    public static void commitText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        for (int offset = 0; offset < text.length(); ) {
            final int codePoint = text.codePointAt(offset);
            if (codePoint < 128) {
                SDLInputConnection.nativeGenerateScancodeForUnichar((char) codePoint);
            }
            offset += Character.charCount(codePoint);
        }
        SDLInputConnection.nativeCommitText(text, 0);
    }

    public static void backspace() {
        SDLInputConnection.nativeGenerateScancodeForUnichar('\b');
    }
}
