package pw.janyo.whatanime.module

import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pw.janyo.whatanime.Application
import pw.janyo.whatanime.context
import pw.janyo.whatanime.db.AppDatabase
import pw.janyo.whatanime.ui.components.Media3CachedPlaybackDataSourceFactory
import pw.janyo.whatanime.ui.components.Media3PlayerComponent
import pw.janyo.whatanime.ui.components.PlaybackStateController

@OptIn(UnstableApi::class)
actual fun platformModule(): Module = module {
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    single { Media3PlayerComponent(context as Application, get()) }
    single { PlaybackStateController(get()) }
    single { Media3CachedPlaybackDataSourceFactory(androidContext()) }

    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(get())
            .setDriver(AndroidSQLiteDriver())
    }
}