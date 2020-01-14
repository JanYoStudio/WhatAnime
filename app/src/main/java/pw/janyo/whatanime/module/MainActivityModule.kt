package pw.janyo.whatanime.module

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.handler.MainItemListener
import pw.janyo.whatanime.ui.activity.MainActivity
import pw.janyo.whatanime.ui.adapter.MainRecyclerAdapter
import pw.janyo.whatanime.viewModel.MainViewModel

val mainActivityModule = module {
	scope(named<MainActivity>()) {
		scoped<ExoPlayer> { (context: MainActivity) ->
			ExoPlayerFactory.newSimpleInstance(context)
		}
		scoped { (context: MainActivity, viewModel: MainViewModel) ->
			MainRecyclerAdapter(context, MainItemListener(viewModel))
		}
	}
}