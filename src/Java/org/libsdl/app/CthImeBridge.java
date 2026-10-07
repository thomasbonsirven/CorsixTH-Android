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

    /**
     * Forward committed IME text through SDL's text-input path only.
     *
     * Do not also synthesize key/scancode events here. SDL's
     * nativeCommitText() emits SDL_TEXTINPUT, which is the event CorsixTH
     * textboxes consume. Sending both a generated scancode and SDL_TEXTINPUT
     * for the same character can make the textbox/IME composition state drift
     * after the first character (notably with Gboard and other composing IMEs).
     */
    public static void commitText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        SDLInputConnection.nativeCommitText(text, 1);
    }

    /**
     * Backspace is intentionally a key event: CorsixTH handles deletion in its
     * textbox key-down path, while normal character entry uses SDL_TEXTINPUT.
     */
    public static void backspace() {
        SDLInputConnection.nativeGenerateScancodeForUnichar('\b');
    }
}
