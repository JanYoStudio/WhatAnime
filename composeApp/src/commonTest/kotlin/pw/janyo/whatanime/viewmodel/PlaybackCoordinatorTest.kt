package pw.janyo.whatanime.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pw.janyo.whatanime.model.PlaybackPhase

class PlaybackCoordinatorTest {
    private class Driver : PlaybackDriver {
        var url = ""
        var plays = 0
        var closes = 0
        override fun load(url: String) { this.url = url }
        override fun play() { plays++ }
        override fun close() { closes++ }
    }

    @Test fun knownExpiredVideoDoesNotCreateOrLoadPlayer() {
        var factories = 0
        val coordinator = PlaybackCoordinator({ factories++; Driver() }, { 601001 })
        assertFalse(coordinator.play("https://example.invalid/v", 1000))
        assertEquals(0, factories)
        assertEquals(PlaybackPhase.Closed, coordinator.state.value.phase)
        assertTrue(coordinator.expiredNotice.value)
    }

    @Test fun closeInvalidatesLateCallbacksAndIsIdempotent() {
        val driver = Driver()
        var callback: (PlaybackEvent) -> Unit = {}
        val coordinator = PlaybackCoordinator({ callback = it; driver })
        coordinator.play("https://example.invalid/v?token=a")
        assertEquals("https://example.invalid/v?token=a&size=l", driver.url)
        coordinator.close()
        coordinator.close()
        callback(PlaybackEvent.Failed)
        assertEquals(PlaybackPhase.Closed, coordinator.state.value.phase)
        assertEquals(1, driver.closes)
    }

    @Test fun oldPlayerCannotFailNewPlayer() {
        val callbacks = mutableListOf<(PlaybackEvent) -> Unit>()
        val coordinator = PlaybackCoordinator({ callbacks += it; Driver() })
        coordinator.play("https://example.invalid/one")
        coordinator.play("https://example.invalid/two")
        callbacks[0](PlaybackEvent.Failed)
        assertEquals(PlaybackPhase.Loading, coordinator.state.value.phase)
        callbacks[1](PlaybackEvent.Ready)
        assertEquals(PlaybackPhase.Playing, coordinator.state.value.phase)
    }

    @Test fun retryRechecksExpiryRatherThanReusingOldBoolean() {
        var time = 1000L
        var factories = 0
        val coordinator = PlaybackCoordinator({ factories++; Driver() }, { time })
        coordinator.play("https://example.invalid/v", 1000)
        time = 601001
        assertFalse(coordinator.retry())
        assertEquals(1, factories)
        assertEquals(PlaybackPhase.Closed, coordinator.state.value.phase)
    }
}
