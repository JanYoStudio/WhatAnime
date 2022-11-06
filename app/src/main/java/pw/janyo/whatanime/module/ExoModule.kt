package pw.janyo.whatanime.module

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val exoModule = module {
    single<DataSource.Factory> {
        DefaultDataSource.Factory(
            androidContext(),
            DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(androidContext(), "WhatAnime"))
        )
    }
}