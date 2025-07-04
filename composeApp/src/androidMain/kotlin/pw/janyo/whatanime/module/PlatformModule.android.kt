package pw.janyo.whatanime.module

import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pw.janyo.whatanime.db.AppDatabase

actual fun platformModule(): Module = module {
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(get())
            .setDriver(AndroidSQLiteDriver())
    }
}