package pw.janyo.whatanime.module

import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@androidx.annotation.OptIn(UnstableApi::class)
val exoModule = module {
    single<DataSource.Factory> {
        DefaultDataSource.Factory(
            androidContext(),
            DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(androidContext(), "WhatAnime"))
        )
    }
}