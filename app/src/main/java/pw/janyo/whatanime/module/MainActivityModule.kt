package pw.janyo.whatanime.module

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.ui.activity.MainActivity

val mainActivityModule = module {
	scope(named<MainActivity>()) {
		scoped<ExoPlayer> { (context: MainActivity) ->
			ExoPlayerFactory.newSimpleInstance(context)
		}
	}
}