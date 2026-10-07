package uk.co.armedpineapple.cth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog

/**
 * Manages analytics and diagnostics reporting consent.
 *
 * Community builds never prompt and never enable telemetry.
 */
class Reporting(private val context: Context) {

    /**
     * Whether consent has been granted by the user.
     *
     * @returns true if consent granted.
     */
    fun hasRequestedConsent(): Boolean {
        if (BuildConfig.COMMUNITY_BUILD) {
            return true
        }
        return context.defaultSharedPreferences.getBoolean(
            context.getString(R.string.prefs_has_requested_consent), false
        )
    }

    /**
     * Opens the privacy policy
     */
    fun openPrivacyPolicy() {
        val url = "https://www.armedpineapple.co.uk/projects/corsixth-for-android/corsixth-for-android-privacy-policy"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.addFlags(FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Log.w("Reporting", "No https viewer available for privacy policy", e)
        }
    }

    /**
     * Requests consent for collection
     *
     * @param activity An activity to own the dialog.
     */
    fun requestConsent(activity: Activity) {
        if (BuildConfig.COMMUNITY_BUILD) {
            return
        }
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_consent, null, false)
        layout.findViewById<Button>(R.id.privacy_policy_button).setOnClickListener {
            openPrivacyPolicy()
        }
        AlertDialog.Builder(activity).setView(layout)
            .setTitle(context.getString(R.string.diagnostics_and_analytics))
            .setPositiveButton(context.getString(R.string.accept)) { _, _ ->
                onConsentChanged(
                    true
                )
            }.setNegativeButton(context.getString(R.string.decline)) { _, _ ->
                onConsentChanged(
                    false
                )
            }.setCancelable(false).create().show()
    }

    private fun onConsentChanged(consent: Boolean) {
        val sharedPreferences = context.defaultSharedPreferences
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.prefs_has_requested_consent), true)
            .putBoolean(context.getString(R.string.prefs_consent), consent).apply()

        Diagnostics.reporter.setCollectionEnabled(consent)
    }
}
