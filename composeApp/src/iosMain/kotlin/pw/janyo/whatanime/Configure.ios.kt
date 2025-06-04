package pw.janyo.whatanime

actual inline fun <reified T> getConfiguration(key: String, defaultValue: T): T {
    return defaultValue
}

actual inline fun <reified T> setConfiguration(key: String, value: T) {
}