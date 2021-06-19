package pw.janyo.whatanime.base

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.LayoutRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import com.orhanobut.logger.Logger
import pw.janyo.whatanime.R

abstract class BaseComposeActivity<V : ComposeViewModel>(
    @LayoutRes val contentLayoutId: Int = 0
) :
    ComponentActivity(contentLayoutId) {
    abstract val viewModel: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuildContent()
        }
        viewModel.exceptionData.observe(this, {
            it?.let {
                Logger.wtf("observe: ", it)
                it.toastLong()
            }
        })
    }

    @Composable
    abstract fun BuildContent()

    @Composable
    protected fun BuildAppBar(
        homePage: Boolean = false,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val colors = MaterialTheme.colors
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            if (homePage) {
                                Icon(Icons.Filled.Menu, "")
                            } else {
                                Icon(Icons.Filled.ArrowBack, "")
                            }
                        }
                    },
                    backgroundColor = colors.primary,
                    contentColor = colors.onPrimary,
                )
            }, content = content
        )
    }

    fun Throwable?.toast() = dispatch(this, false)
    fun Throwable?.toastLong() = dispatch(this, true)
    fun String?.toast() = newToast(this@BaseComposeActivity, this, Toast.LENGTH_SHORT)
    fun String?.toastLong() = newToast(this@BaseComposeActivity, this, Toast.LENGTH_LONG)

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
        Toast.makeText(context, message, length).show()
    }
}