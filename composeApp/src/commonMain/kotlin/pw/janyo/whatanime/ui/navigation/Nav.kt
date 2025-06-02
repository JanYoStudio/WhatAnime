package pw.janyo.whatanime.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pw.janyo.whatanime.ui.screen.AboutScreen
import pw.janyo.whatanime.ui.screen.DetailScreen
import pw.janyo.whatanime.ui.screen.HistoryScreen
import pw.janyo.whatanime.ui.screen.MainScreen
import pw.janyo.whatanime.ui.screen.SettingsScreen

val LocalNavController = compositionLocalOf<NavController?> { null }

@Serializable
object RouteMain

@Serializable
object RouteAbout

@Serializable
object RouteHistory

@Serializable
data class RouteDetail(val historyId: Int, val cachePath: String)

@Serializable
object RouteSettings

val Navs: NavGraphBuilder.() -> Unit = {
    composable<RouteMain> { MainScreen() }
    composable<RouteAbout> { AboutScreen() }
    composable<RouteHistory> { HistoryScreen() }
    composable<RouteDetail> { backStackEntry ->
        val detail: RouteDetail = backStackEntry.toRoute()
        DetailScreen(detail.historyId, detail.cachePath)
    }
    composable<RouteSettings> { SettingsScreen() }
}