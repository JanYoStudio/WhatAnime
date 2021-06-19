package pw.janyo.whatanime.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.ui.activity.HistoryActivity
import pw.janyo.whatanime.ui.adapter.HistoryRecyclerAdapter

val historyActivityModule = module {
    scope(named<HistoryActivity>()) {
        scoped { HistoryItemListener(get()) }
        scoped { HistoryRecyclerAdapter() }
    }
}