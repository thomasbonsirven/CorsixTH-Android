package uk.co.armedpineapple.cth

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.core.view.WindowCompat

/**
 * Phase 8 helpers for Android 14/15/16 window / cutout behavior.
 *
 * Game surfaces stay edge-to-edge (SDL immersive). Non-game activities keep
 * content laid out inside system bars when edge-to-edge is enforced.
 */
object AndroidUiHardening {

    fun applyGameDisplayCutout(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrs = activity.window.attributes
            attrs.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            activity.window.attributes = attrs
        }
    }

    fun applyNonGameSystemBars(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
    }
}
