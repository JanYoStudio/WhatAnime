package pw.janyo.whatanime.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import pw.janyo.whatanime.trackEvent

fun Application.registerActivityLifecycle() {
    registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            trackEvent("打开 ${activity.javaClass.simpleName}:${activity.title}")
        }
    })
}