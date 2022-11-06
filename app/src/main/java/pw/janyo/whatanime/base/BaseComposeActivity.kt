package pw.janyo.whatanime.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.core.component.KoinComponent
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import java.util.*
import kotlin.reflect.KClass

abstract class BaseComposeActivity(
    private val setSystemUiColor: Boolean = true,
    private val registerEventBus: Boolean = false,
) :
    ComponentActivity(), KoinComponent {
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
        setContent {
            BuildContent()
        }
    }

    open fun initIntent() {}

    @Composable
    open fun BuildContentWindow() {
        WhatAnimeTheme {
            if (setSystemUiColor) {
                val systemUiController = rememberSystemUiController()
                val systemBarColor = MaterialTheme.colors.primary
                val isLight = MaterialTheme.colors.isLight
                SideEffect {
                    systemUiController.setSystemBarsColor(systemBarColor, darkIcons = isLight)
                    systemUiController.setNavigationBarColor(systemBarColor, darkIcons = isLight)
                }
            }
            BuildContent()
        }
    }

    @Composable
    open fun BuildContent() {
    }

    override fun attachBaseContext(newBase: Context) {
        val language = Configure.language
        if (language == 0) {
            super.attachBaseContext(newBase)
            return
        }
        super.attachBaseContext(changeLanguage(newBase))
    }

    @SuppressLint("NewApi")
    private fun changeLanguage(context: Context): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val newLocale: Locale = when (Configure.language) {
                1 -> Locale.SIMPLIFIED_CHINESE
                2 -> Locale.TRADITIONAL_CHINESE
                3 -> Locale.forLanguageTag("zh-Hant-HK")
                else -> Locale.getDefault()
            }
            val configuration = Resources.getSystem().configuration
            configuration.setLocale(newLocale)
            configuration.setLocales(LocaleList(newLocale))
            context.createConfigurationContext(configuration)
        } else {
            context
        }
    }

    fun <T : Activity> intentTo(
        clazz: KClass<T>,
        block: Intent.() -> Unit = {},
    ) {
        startActivity(Intent(this, clazz.java).apply(block))
    }

    fun String.toast(showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            this,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    fun toastString(message: String, showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            message,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    protected fun String.notBlankToast(showLong: Boolean = false) {
        if (this.isNotBlank()) {
            newToast(
                this@BaseComposeActivity,
                this,
                if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            )
        }
    }

    fun @receiver:StringRes Int.toast(showLong: Boolean = false) =
        asString().toast(showLong)

    private fun newToast(context: Context, message: String?, length: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, message, length)
        toast?.show()
    }

    fun @receiver:StringRes Int.asString(): String = getString(this)

    @Composable
    protected fun ShowProgressDialog(
        show: Boolean,
        text: String,
        fontSize: TextUnit = TextUnit.Unspecified,
    ) {
        if (!show) {
            return
        }
        Dialog(
            onDismissRequest = { },
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp),
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = text,
                        fontSize = fontSize,
                        color = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    }
}