package pw.janyo.whatanime.handler

import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.viewModel.MainViewModel

class MainItemListener(
		private val mainViewModel: MainViewModel
) {
	fun click(docs: Docs) {
		mainViewModel.playVideo(docs)
	}
}