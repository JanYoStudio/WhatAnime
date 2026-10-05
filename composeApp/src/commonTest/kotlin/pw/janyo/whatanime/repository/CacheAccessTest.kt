package pw.janyo.whatanime.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CacheAccessTest {
    @Test fun deletionCannotRaceAWholeSearchCacheTransaction() = runTest {
        val repository = AnimationRepository()
        val release = CompletableDeferred<Unit>()
        val steps = mutableListOf<String>()
        launch { repository.withCacheOperation { steps += "search"; release.await(); steps += "saved" } }
        launch { repository.withCacheOperation { steps += "delete" } }
        runCurrent()
        assertEquals(listOf("search"), steps)
        release.complete(Unit)
        runCurrent()
        assertEquals(listOf("search", "saved", "delete"), steps)
    }
}
