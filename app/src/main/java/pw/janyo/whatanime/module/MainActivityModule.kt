package pw.janyo.whatanime.module

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.handler.MainItemListener
import pw.janyo.whatanime.ui.activity.MainActivity
import pw.janyo.whatanime.ui.adapter.MainRecyclerAdapter

val mainActivityModule = module {
    scope(named<MainActivity>()) {
        scoped<ExoPlayer> {
            SimpleExoPlayer.Builder(get())
                .build()
        }
        scoped { MainItemListener() }
        scoped {
            MainRecyclerAdapter(get(), get())
        }
    }
}