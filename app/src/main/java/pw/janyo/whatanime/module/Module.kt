package pw.janyo.whatanime.module

import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun moduleList(): List<Module> =
    listOf(
        appModule,
        databaseModule,
        networkModule,
        repositoryModule,
        exoModule,
    )

private val appModule = module {
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
}