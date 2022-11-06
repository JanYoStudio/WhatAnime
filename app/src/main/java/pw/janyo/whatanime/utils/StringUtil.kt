package pw.janyo.whatanime.utils

import android.util.Base64
import java.security.MessageDigest

fun String.md5(): String = toByteArray().md5()

/**
 * MD5 加密
 * @return MD5 加密之后的字符串
 */
fun ByteArray.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    val result = digest.digest(this)
    return toHex(result)
}

fun String.sha1(): String = toByteArray().sha1()

/**
 * SHA-1 加密
 * @return SHA-1 加密之后的字符串
 */
fun ByteArray.sha1(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val result = digest.digest(this)
    return toHex(result)
}

fun String.sha256(): String = toByteArray().sha256()

/**
 * SHA-256 加密
 * @return SHA-256 加密之后的字符串
 */
fun ByteArray.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val result = digest.digest(this)
    return toHex(result)
}

/**
 * 将指定byte数组转换为16进制字符串
 * @param byteArray 原始数据
 * @return 转换之后数据
 */
internal fun toHex(byteArray: ByteArray): String =
    with(StringBuilder()) {
        byteArray.forEach {
            val value = it
            val hex = value.toInt() and (0xFF)
            val hexStr = Integer.toHexString(hex)
            if (hexStr.length == 1)
                append("0").append(hexStr)
            else
                append(hexStr)
        }
        toString()
    }

fun ByteArray.base64(flags: Int = Base64.DEFAULT): ByteArray = Base64.encode(this, flags)
fun ByteArray.deBase64(flags: Int = Base64.DEFAULT): ByteArray = Base64.decode(this, flags)
fun ByteArray.base64String(flags: Int = Base64.DEFAULT): String = String(base64(flags))
fun ByteArray.deBase64String(flags: Int = Base64.DEFAULT): String = String(deBase64(flags))
fun String.base64(flags: Int = Base64.DEFAULT): String = toByteArray().base64String(flags)
fun String.deBase64(flags: Int = Base64.DEFAULT): String = toByteArray().deBase64String(flags)