package uk.co.armedpineapple.cth

/**
 * Optional crash / diagnostics sink.
 *
 * Community builds use [NoOpCrashReporter] (no telemetry).
 * Play Store builds install a Firebase-backed implementation at startup.
 */
interface CrashReporter {
    fun log(message: String)
    fun recordException(throwable: Throwable)
    fun setCollectionEnabled(enabled: Boolean)
}

object NoOpCrashReporter : CrashReporter {
    override fun log(message: String) = Unit
    override fun recordException(throwable: Throwable) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

object Diagnostics {
    @JvmField
    var reporter: CrashReporter = NoOpCrashReporter
}
