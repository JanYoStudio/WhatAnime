package pw.janyo.whatanime.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute

internal fun NavDestination.isTopLevelPage() =
    hasRoute<RouteMain>() || hasRoute<RouteHistory>() || hasRoute<RouteSettings>()

// 与 NodeFlow 相同的前景滑入／背景视差。Navigation 2.9.2 会对 pop 转场
// 按返回手势进度 seek，无需套额外的计时动画，也无需升级导航依赖。
internal fun pageEnter(): EnterTransition =
    slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } +
        fadeIn(tween(90)) + scaleIn(tween(300, easing = FastOutSlowInEasing), initialScale = 0.96f)

internal fun pageExit(): ExitTransition =
    slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it / 4 } +
        fadeOut(tween(90, delayMillis = 120)) + scaleOut(tween(260, easing = FastOutSlowInEasing), targetScale = 0.92f)

internal fun pagePopEnter(): EnterTransition =
    slideInHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it / 4 } +
        fadeIn(tween(90)) + scaleIn(tween(260, easing = FastOutSlowInEasing), initialScale = 0.92f)

internal fun pagePopExit(): ExitTransition =
    slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } +
        fadeOut(tween(90, delayMillis = 210)) + scaleOut(tween(300, easing = FastOutSlowInEasing), targetScale = 0.90f)
