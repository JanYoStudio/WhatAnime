package pw.janyo.whatanime.ui.components

enum class NavigationKind { Bottom, Rail, CompactRail }

data class LayoutSpec(
    val navigation: NavigationKind,
    val railWidth: Float,
    val dualPane: Boolean,
    val verticalCards: Boolean,
    val stickyImage: Boolean,
)

/** 参数是已扣除系统安全区、尚未扣导航宽度的窗口 dp 约束。 */
fun layoutFor(widthDp: Float, heightDp: Float, fontScale: Float, showNavigation: Boolean = true): LayoutSpec {
    val short = heightDp < 480f
    val navigation = when {
        short -> NavigationKind.CompactRail
        widthDp >= 600f -> NavigationKind.Rail
        else -> NavigationKind.Bottom
    }
    val rail = if (!showNavigation) 0f else when (navigation) {
        NavigationKind.Bottom -> 0f
        NavigationKind.CompactRail -> if (widthDp < 480f) 56f else 72f
        NavigationKind.Rail -> if (fontScale >= 1.5f) 104f else 80f
    }
    val content = (widthDp - rail - 32f).coerceAtLeast(0f)
    val dual = !short && navigation == NavigationKind.Rail && content >= 784f
    val cardWidth = if (dual) (content - 24f) / 2f else content
    return LayoutSpec(
        navigation, rail, dual,
        cardWidth < 360f || (fontScale >= 1.5f && cardWidth < 400f),
        !short,
    )
}
