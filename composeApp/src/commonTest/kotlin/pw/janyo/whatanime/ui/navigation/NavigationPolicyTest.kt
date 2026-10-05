package pw.janyo.whatanime.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationPolicyTest {
    @Test fun backClosesOverlayBeforePoppingDetail() {
        assertEquals(BackAction.CloseOverlay, backAction(true, true, TopLevelDestination.History))
    }
    @Test fun detailPopsBeforeReturningToSearch() {
        assertEquals(BackAction.PopDetail, backAction(false, true, TopLevelDestination.History))
    }
    @Test fun rootReturnsToSearchThenSystem() {
        assertEquals(BackAction.Search, backAction(false, false, TopLevelDestination.Settings))
        assertEquals(BackAction.System, backAction(false, false, TopLevelDestination.Search))
    }
}
