package pw.janyo.whatanime

import com.tencent.mmkv.MMKV

val kv = MMKV.mmkvWithID("configure")

actual inline fun <reified T> getConfiguration(key: String, defaultValue: T): T =
    when (defaultValue) {
        is Boolean -> kv.decodeBool(key, defaultValue)
        is Int -> kv.decodeInt(key, defaultValue)
        is Long -> kv.decodeLong(key, defaultValue)
        is Float -> kv.decodeFloat(key, defaultValue)
        is String -> kv.decodeString(key, defaultValue)
        else -> throw IllegalArgumentException("Unsupported type")
    } as T

actual inline fun <reified T> setConfiguration(key: String, value: T) {
    when(value){
        is Boolean -> kv.encode(key, value)
        is Int -> kv.encode(key, value)
        is Long -> kv.encode(key, value)
        is Float -> kv.encode(key, value)
        is String -> kv.encode(key, value)
        else -> throw IllegalArgumentException("Unsupported type")
    }
}