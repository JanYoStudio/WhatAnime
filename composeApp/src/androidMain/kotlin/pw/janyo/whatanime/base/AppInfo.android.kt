package pw.janyo.whatanime.base

import android.annotation.SuppressLint
import android.provider.Settings
import pw.janyo.whatanime.BuildConfig
import pw.janyo.whatanime.R
import pw.janyo.whatanime.context

//设备id
val publicDeviceId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

//应用名称
val appName: String
    get() = context.getString(R.string.app_name)

//应用包名
const val packageName: String = BuildConfig.APPLICATION_ID

//版本名称
const val appVersionName: String = BuildConfig.VERSION_NAME

//版本号
const val appVersionCode: String = BuildConfig.VERSION_CODE.toString()
const val appVersionCodeNumber: Long = BuildConfig.VERSION_CODE.toLong()

actual fun publicDeviceId(): String = publicDeviceId

actual fun appName(): String = appName

actual fun packageName(): String = packageName

actual fun appVersionName(): String = appVersionName

actual fun appVersionCode(): String = appVersionCode

actual fun appVersionCodeNumber(): Long = appVersionCodeNumber