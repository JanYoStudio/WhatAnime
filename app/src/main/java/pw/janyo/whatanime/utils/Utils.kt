package pw.janyo.whatanime.utils

fun <T> firstNotNull(defaultValue: T, vararg data: T?): T {
    val firstNotNullOfOrNull = data.firstNotNullOfOrNull { it }
    return firstNotNullOfOrNull ?: defaultValue
}