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

actual fun publicDeviceId(): String = getOrCreateDeviceUniqueId()

actual fun appName(): String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleDisplayName") as? String
        ?: NSBundle.mainBundle.infoDictionary?.get("CFBundleName") as? String
        ?: "Unknown"

actual fun packageName(): String = NSBundle.mainBundle.bundleIdentifier ?: "Unknown"

actual fun appVersionName(): String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "Unknown"

actual fun appVersionCode(): String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: "Unknown"

actual fun appVersionCodeNumber(): Long = runCatching { appVersionCode().toLong() }.getOrDefault(1L)