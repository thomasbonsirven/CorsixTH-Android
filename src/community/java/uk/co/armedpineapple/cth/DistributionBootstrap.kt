package uk.co.armedpineapple.cth

import android.app.Activity
import android.app.Application
import uk.co.armedpineapple.cth.stats.StatisticsService

/**
 * Community distribution: no Firebase, no Play Games, no telemetry.
 */
object DistributionBootstrap {
    fun init(@Suppress("UNUSED_PARAMETER") app: Application) {
        Diagnostics.reporter = NoOpCrashReporter
    }

    fun createPlayGames(
        @Suppress("UNUSED_PARAMETER") activity: Activity,
        @Suppress("UNUSED_PARAMETER") statisticsService: StatisticsService
    ): PlayGamesController? = null
}
