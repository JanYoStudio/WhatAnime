package pw.janyo.whatanime.base

import platform.Foundation.NSBundle
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice
import pw.janyo.whatanime.getConfiguration
import pw.janyo.whatanime.setConfiguration

private fun getOrCreateDeviceUniqueId(): String {
    var uniqueId = getConfiguration<String?>("device_unique_id", null)
    if (uniqueId == null) {
        // 首次安装或数据清除后生成新的ID
        // identifierForVendor 是一个很好的选择，因为它在同一厂商的应用间保持一致
        uniqueId = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: NSUUID().UUIDString
        setConfiguration("device_unique_id", uniqueId)
    }
    return uniqueId
}

//设备id
val publicDeviceId: String by lazy { getOrCreateDeviceUniqueId() }

//应用名称
val appName: String by lazy {
    NSBundle.mainBundle.infoDictionary?.get("CFBundleDisplayName") as? String
        ?: NSBundle.mainBundle.infoDictionary?.get("CFBundleName") as? String
        ?: "Unknown"
}

//应用包名
val packageName: String by lazy {
    NSBundle.mainBundle.bundleIdentifier ?: "Unknown"
}

//版本名称
val appVersionName: String by lazy {
    NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        ?: "Unknown"
}

//版本号
val appVersionCode: String by lazy {
    NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: "Unknown"
}
val appVersionCodeNumber: Long
    get() = runCatching { appVersionCode.toLong() }.getOrDefault(1L)

actual fun publicDeviceId(): String = publicDeviceId

actual fun appName(): String = appName

actual fun packageName(): String = packageName

actual fun appVersionName(): String = appVersionName

actual fun appVersionCode(): String = appVersionCode

actual fun appVersionCodeNumber(): Long = appVersionCodeNumber