package pw.janyo.whatanime.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.ui.screen.AboutScreen
import pw.janyo.whatanime.ui.screen.DetailScreen
import pw.janyo.whatanime.ui.screen.HistoryScreen
import pw.janyo.whatanime.ui.screen.MainScreen
import pw.janyo.whatanime.ui.screen.SettingsScreen

val LocalNavController = compositionLocalOf<NavController?> { null }

@Serializable object RouteRoot
@Serializable object RouteMain
@Serializable object RouteAbout
@Serializable object RouteHistory
@Serializable data class RouteDetail(val historyId: Int, val cachePath: String)
@Serializable object RouteSettings

fun NavGraphBuilder.appNavGraph(navController: NavController) {
    navigation<RouteRoot>(startDestination = RouteMain) {
        composable<RouteMain> { entry ->
            val root = remember(entry) { navController.getBackStackEntry<RouteRoot>() }
            MainScreen(koinViewModel(viewModelStoreOwner = root))
        }
        composable<RouteHistory> { entry ->
            val root = remember(entry) { navController.getBackStackEntry<RouteRoot>() }
            HistoryScreen(koinViewModel(viewModelStoreOwner = root))
        }
        composable<RouteSettings> { entry ->
            val root = remember(entry) { navController.getBackStackEntry<RouteRoot>() }
            SettingsScreen(koinViewModel(viewModelStoreOwner = root))
        }
        composable<RouteAbout> { AboutScreen() }
        composable<RouteDetail> { entry ->
            val detail: RouteDetail = entry.toRoute()
            DetailScreen(detail.historyId, detail.cachePath)
        }
    }
}

fun NavController.selectTopLevel(destination: TopLevelDestination) {
    val alreadySelected = when (destination) {
        TopLevelDestination.Search -> currentDestination?.hasRoute<RouteMain>() == true
        TopLevelDestination.History -> currentDestination?.hasRoute<RouteHistory>() == true
        TopLevelDestination.Settings -> currentDestination?.hasRoute<RouteSettings>() == true
    }
    if (alreadySelected) return
    val options: androidx.navigation.NavOptionsBuilder.() -> Unit = {
        popUpTo<RouteMain> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    when (destination) {
        TopLevelDestination.Search -> navigate(RouteMain, options)
        TopLevelDestination.History -> navigate(RouteHistory, options)
        TopLevelDestination.Settings -> navigate(RouteSettings, options)
    }
}
