package pw.janyo.whatanime.module

import android.content.ClipboardManager
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
	single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
}