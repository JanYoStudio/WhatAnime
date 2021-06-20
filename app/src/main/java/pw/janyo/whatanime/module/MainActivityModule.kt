package pw.janyo.whatanime.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.ui.activity.MainActivity

val mainActivityModule = module {
    scope(named<MainActivity>()) {
    }
}