package pw.janyo.whatanime.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.animation.doOnEnd
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.registerAppCenter

class StartActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAppCenter(application)
    }

    @Composable
    override fun BuildContent() {
        goToMainScreen()
    }

    @RequiresApi(31)
    override fun initIntent() {
        super.initIntent()
        customizeSplashScreenExit()
    }

    private fun goToMainScreen() {
        intentTo(MainActivity::class)
        finish()
    }

    @RequiresApi(31)
    private fun customizeSplashScreenExit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        splashScreen.setOnExitAnimationListener { view ->
            val iconView = view.iconView ?: return@setOnExitAnimationListener
            AnimatorSet().apply {
                playSequentially(
                    ObjectAnimator.ofFloat(iconView, View.TRANSLATION_Y, 0f, 50f),
                    ObjectAnimator.ofFloat(
                        iconView,
                        View.TRANSLATION_Y,
                        50f,
                        -view.height.toFloat()
                    ),
                )
                doOnEnd { view.remove() }
                start()
            }
        }
    }
}