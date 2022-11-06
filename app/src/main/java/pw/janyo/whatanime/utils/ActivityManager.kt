package pw.janyo.whatanime.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import pw.janyo.whatanime.trackEvent
import java.util.Stack

private val activityStack by lazy { Stack<Activity>() }

/**
 * 添加Activity到堆栈
 */
private fun addActivity(activity: Activity?) = activityStack.add(activity)

/**
 * 移除Activity到堆栈
 */
private fun removeActivity(activity: Activity?) = activityStack.remove(activity)

/**
 * 结束所有Activity
 */
fun finishAllActivity() {
    activityStack.forEach {
        it?.finish()
    }
    activityStack.clear()
}

fun Application.registerActivityLifecycle() {
    registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityDestroyed(activity: Activity) {
            removeActivity(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            addActivity(activity)
            trackEvent("打开 ${activity.javaClass.simpleName}:${activity.title}")
        }
    })
}