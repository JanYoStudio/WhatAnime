package pw.janyo.whatanime.config

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object DispatcherConfig {
    val CHECK_NETWORK = Executors.newFixedThreadPool(3).asCoroutineDispatcher()
    val NETWORK = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
}