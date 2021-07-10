package pw.janyo.whatanime.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.LocaleList
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pw.janyo.whatanime.R
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.debugMode
import vip.mystery0.tools.utils.AndroidVersionCode
import vip.mystery0.tools.utils.sdkIsAfter
import java.util.*
import kotlin.reflect.KClass

typealias Action = () -> Unit

abstract class BaseComposeActivity<V : ComposeViewModel>(
    @LayoutRes val contentLayoutId: Int = 0
) :
    ComponentActivity(contentLayoutId) {
    private var toast: Toast? = null
    private var debugFlow: MutableStateFlow<Pair<String, Action>?> = MutableStateFlow(null)
    abstract val viewModel: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
        setContent {
            BuildContent()
            val debug by debugMode.collectAsState()
            if (debug) {
                BuildDebug()
            }
        }
    }

    protected fun observerErrorMessage(block: (String) -> Unit) {
        viewModel.exceptionData.observe(this, { e ->
            e?.let { throwable ->
                Logger.e(throwable, "exception detected")
                throwable.message?.let {
                    block(it)
                }
                viewModel.clear()
            }
        })
        viewModel.errorMessageData.observe(this, {
            it?.let {
                block(it)
                viewModel.clear()
            }
        })
    }

    open fun initIntent() {}

    @Composable
    abstract fun BuildContent()

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
        return if (sdkIsAfter(AndroidVersionCode.VERSION_O)) {
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

    fun <T : Activity> intentTo(clazz: KClass<T>) {
        startActivity(Intent(this, clazz.java))
    }

    fun String.toast() = newToast(this@BaseComposeActivity, this, Toast.LENGTH_SHORT)
    fun String.toastLong() = newToast(this@BaseComposeActivity, this, Toast.LENGTH_LONG)

    private fun dispatch(
        throwable: Throwable?,
        isLong: Boolean
    ) {
        newToast(
            this,
            throwable?.message
                ?: getString(R.string.hint_unknow_error),
            if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )
    }

    private fun newToast(context: Context, message: String?, length: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, message, length)
        toast?.show()
    }

    fun @receiver:StringRes Int.asString(): String = getString(this)

    @Composable
    private fun BuildDebug() {
        val actionState by debugFlow.collectAsState()
        actionState?.let {
            AlertDialog(
                onDismissRequest = { debugFlow.value = null },
                confirmButton = {
                    TextButton(onClick = {
                        it.second()
                        debugFlow.value = null
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                title = {
                    Text(text = "调试模式")
                }, text = {
                    SelectionContainer {
                        Text(text = it.first)
                    }
                }
            )
        }
    }

    protected fun debugOnClick(message: String, action: () -> Unit) {
        if (debugMode.value) {
            viewModel.viewModelScope.launch {
                debugFlow.emit(message to action)
            }
        } else {
            action()
        }
    }
}