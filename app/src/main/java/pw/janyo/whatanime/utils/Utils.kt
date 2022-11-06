package pw.janyo.whatanime.utils

fun <T> firstNotNull(defaultValue: T, vararg data: T?): T {
    val firstNotNullOfOrNull = data.firstNotNullOfOrNull { it }
    return firstNotNullOfOrNull ?: defaultValue
}

fun firstNotBlank(defaultValue: String, vararg data: String?): String =
    firstNotBlank(defaultValue, dataList = listOf(elements = data))

fun firstNotBlank(defaultValue: String, dataList: List<String?>): String {
    val firstNotBlank = dataList.firstOrNull { !it.isNullOrBlank() }
    return firstNotBlank ?: defaultValue
}