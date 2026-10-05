package pw.janyo.whatanime.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveLayoutTest {
    @Test fun smallPhonesKeepNavigationAndVerticalCards() {
        val small = layoutFor(320f, 640f, 1f)
        assertEquals(NavigationKind.Bottom, small.navigation)
        assertTrue(small.verticalCards)
        assertFalse(small.dualPane)
    }
    @Test fun railWidthIsRemovedBeforeDualPaneDecision() {
        assertFalse(layoutFor(840f, 900f, 1f).dualPane)
        assertFalse(layoutFor(895f, 900f, 1f).dualPane)
        assertTrue(layoutFor(896f, 900f, 1f).dualPane)
    }
    @Test fun secondaryPagesDoNotSubtractHiddenNavigation() {
        assertTrue(layoutFor(840f, 900f, 1f, showNavigation = false).dualPane)
        assertEquals(0f, layoutFor(840f, 900f, 1f, showNavigation = false).railWidth)
    }
    @Test fun shortHeightKeepsThreeDestinationsWithoutStickyImage() {
        val short = layoutFor(840f, 479f, 1f)
        assertEquals(NavigationKind.CompactRail, short.navigation)
        assertEquals(72f, short.railWidth)
        assertFalse(short.stickyImage)
        assertFalse(short.dualPane)
        assertEquals(56f, layoutFor(320f, 300f, 2f).railWidth)
    }
    @Test fun largeTextChoosesVerticalCardsFromAvailableContentWidth() {
        assertTrue(layoutFor(390f, 800f, 1.5f).verticalCards)
        assertFalse(layoutFor(500f, 800f, 1.5f).verticalCards)
        assertEquals(NavigationKind.Rail, layoutFor(600f, 800f, 1f).navigation)
    }
}
