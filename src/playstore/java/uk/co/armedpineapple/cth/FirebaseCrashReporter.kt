package uk.co.armedpineapple.cth

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class FirebaseCrashReporter : CrashReporter {
    override fun log(message: String) {
        Firebase.crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(enabled)
        Firebase.analytics.setAnalyticsCollectionEnabled(enabled)
    }
}
