package pw.janyo.whatanime

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.CachePolicy
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pw.janyo.whatanime.model.PlaybackPhase
import pw.janyo.whatanime.ui.components.*
import pw.janyo.whatanime.ui.navigation.*
import pw.janyo.whatanime.ui.screen.BackHandler
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.viewmodel.PlaybackCoordinator
import whatanime.composeapp.generated.resources.*

val LocalLayoutSpec = compositionLocalOf { layoutFor(400f, 800f, 1f) }

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { addPlatformFileSupport() }
            .memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED).build()
    }
    val navController = rememberNavController()
    val playback = koinInject<PlaybackCoordinator>()
    val playbackState by playback.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    val selected = when {
        destination?.hasRoute<RouteHistory>() == true -> TopLevelDestination.History
        destination?.hasRoute<RouteSettings>() == true -> TopLevelDestination.Settings
        else -> TopLevelDestination.Search
    }
    val topLevel = destination == null || destination.hasRoute<RouteMain>() ||
        destination.hasRoute<RouteHistory>() || destination.hasRoute<RouteSettings>()
    DisposableEffect(lifecycleOwner, playback) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) playback.onBackground()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playback.dispose()
        }
    }
    LaunchedEffect(destination?.route) { playback.close() }
    val rootBackAction = backAction(
        overlay = playbackState.phase != PlaybackPhase.Closed,
        detail = !topLevel,
        destination = selected,
    )
    BackHandler(rootBackAction == BackAction.Search) {
        navController.selectTopLevel(TopLevelDestination.Search)
    }
    WhatAnimeTheme {
        Surface(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                val layout = layoutFor(maxWidth.value, maxHeight.value, LocalDensity.current.fontScale, showNavigation = false)
                CompositionLocalProvider(LocalNavController provides navController, LocalLayoutSpec provides layout) {
                    NavHost(
                        navController = navController,
                        startDestination = RouteRoot,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            if (initialState.destination.isTopLevelPage() && targetState.destination.isTopLevelPage()) EnterTransition.None else pageEnter()
                        },
                        exitTransition = {
                            if (initialState.destination.isTopLevelPage() && targetState.destination.isTopLevelPage()) ExitTransition.None else pageExit()
                        },
                        popEnterTransition = {
                            if (initialState.destination.isTopLevelPage() && targetState.destination.isTopLevelPage()) EnterTransition.None else pagePopEnter()
                        },
                        popExitTransition = {
                            if (initialState.destination.isTopLevelPage() && targetState.destination.isTopLevelPage()) ExitTransition.None else pagePopExit()
                        },
                    ) { appNavGraph(navController) }
                    BuildVideoDialog()
                }
            }
        }
    }
}

// 主入口的导航栏属于该页面，一起参与层级转场；返回手势结束时不再突然增减内容高度。
@Composable
internal fun TopLevelPage(selected: TopLevelDestination, navController: NavController, content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val layout = layoutFor(maxWidth.value, maxHeight.value, LocalDensity.current.fontScale)
            CompositionLocalProvider(LocalLayoutSpec provides layout) {
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        if (layout.navigation != NavigationKind.Bottom) {
                            NavigationRail(Modifier.width(layout.railWidth.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
                                TopLevelDestination.entries.forEach { tab ->
                                    val label = tabLabel(tab)
                                    NavigationRailItem(
                                        selected = selected == tab, onClick = { navController.selectTopLevel(tab) },
                                        icon = { Icon(tabIcon(tab), label) },
                                        label = if (layout.navigation == NavigationKind.CompactRail) null else ({ Text(label) }),
                                    )
                                }
                            }
                        }
                        Box(Modifier.weight(1f).fillMaxHeight()) { content() }
                    }
                    if (layout.navigation == NavigationKind.Bottom) {
                        NavigationBar {
                            TopLevelDestination.entries.forEach { tab ->
                                val label = tabLabel(tab)
                                NavigationBarItem(
                                    selected = selected == tab, onClick = { navController.selectTopLevel(tab) },
                                    icon = { Icon(tabIcon(tab), label) }, label = { Text(label) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun tabLabel(tab: TopLevelDestination): String = stringResource(when (tab) {
    TopLevelDestination.Search -> Res.string.ui_search
    TopLevelDestination.History -> Res.string.action_history
    TopLevelDestination.Settings -> Res.string.action_settings
})

private fun tabIcon(tab: TopLevelDestination) = when (tab) {
    TopLevelDestination.Search -> Icons.Outlined.ImageSearch
    TopLevelDestination.History -> Icons.Outlined.History
    TopLevelDestination.Settings -> Icons.Outlined.Settings
}
