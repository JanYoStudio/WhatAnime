package pw.janyo.whatanime.module

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val exoModule = module {
	single<DataSource.Factory> {
		DefaultDataSourceFactory(androidContext(), Util.getUserAgent(androidContext(), androidContext().packageName))
	}
}