package pw.janyo.whatanime.handler

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.viewModel.MainViewModel

class MainItemListener : KoinComponent {
    private val mainViewModel: MainViewModel by inject()

    fun click(docs: Docs) {
        mainViewModel.playVideo(docs)
    }
}