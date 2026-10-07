package uk.co.armedpineapple.cth

import android.app.Activity
import android.app.Application
import com.google.android.gms.games.PlayGamesSdk
import uk.co.armedpineapple.cth.stats.StatisticsService

/**
 * Play Store distribution: Firebase Crashlytics/Analytics + Play Games.
 */
object DistributionBootstrap {
    fun init(app: Application) {
        Diagnostics.reporter = FirebaseCrashReporter()
        PlayGamesSdk.initialize(app)
    }

    fun createPlayGames(
        activity: Activity,
        statisticsService: StatisticsService
    ): PlayGamesController = PlayGamesService(activity, statisticsService)
}
