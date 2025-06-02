package pw.janyo.whatanime.utils

import kotlinx.datetime.LocalDateTime

enum class TimeUnit(val level: Int, val unit: String, val interval: Int) {
    MILLISECOND(0, "毫秒", 1000),
    SECOND(1, "秒", 60),
    MINUTE(2, "分", 60),
    HOUR(3, "小时", 24),
    DAY(4, "天", 1)
}

private fun getTimeUnitByLevel(level: Int): TimeUnit? = when (level) {
    0 -> TimeUnit.MILLISECOND
    1 -> TimeUnit.SECOND
    2 -> TimeUnit.MINUTE
    3 -> TimeUnit.HOUR
    4 -> TimeUnit.DAY
    else -> null
}

fun Long.formatTime(
    minTimeUnit: TimeUnit = TimeUnit.MILLISECOND,
    maxTimeUnit: TimeUnit = TimeUnit.DAY
): String {
    if (minTimeUnit.level > maxTimeUnit.level) {
        //等级不正确，抛出异常
        throw NumberFormatException("level error")
    }
    val ss = 1000
    val mi = ss * 60
    val hh = mi * 60
    val dd = hh * 24

    if (this <= 0) return "0${minTimeUnit.unit}"
    if (maxTimeUnit == TimeUnit.MILLISECOND) return "$this${TimeUnit.MILLISECOND.unit}"

    val day = this / dd
    val hour = (this - day * dd) / hh
    val minute = (this - day * dd - hour * hh) / mi
    val second = (this - day * dd - hour * hh - minute * mi) / ss
    val milliSecond = this % ss
    val array = arrayOf(day, hour, minute, second, milliSecond)
    val sb = StringBuilder()
    for (index in array.indices) {
        val unit = getTimeUnitByLevel(array.size - index - 1)!!
        val nextUnit = getTimeUnitByLevel(array.size - index - 2)
        if (array[index] > 0) {
            if (maxTimeUnit.level < unit.level) {
                if (nextUnit != null)
                    array[index + 1] += array[index] * nextUnit.interval
            } else {
                sb.append(array[index]).append(unit.unit)
            }
        }
        if (minTimeUnit == unit) {
            if (sb.isEmpty()) sb.append(0).append(minTimeUnit.unit)
            return sb.toString()
        }
    }
    return sb.toString()
}

fun LocalDateTime.formatDateTime(): String = formatWithFormatter("yyyy-MM-dd HH:mm:ss")

private fun LocalDateTime.formatWithFormatter(format: String): String {
    return format.replace("yyyy", year.toString())
        .replace("MM", monthNumber.pad2())
        .replace("M", monthNumber.toString())
        .replace("dd", dayOfMonth.pad2())
        .replace("d", dayOfMonth.toString())
        .replace("HH", hour.pad2())
        .replace("H", hour.toString())
        .replace("hh", if (hour > 12) (hour - 12).pad2() else hour.pad2())
        .replace("h", if (hour > 12) (hour - 12).toString() else hour.toString())
        .replace("mm", minute.pad2())
        .replace("m", minute.toString())
        .replace("ss", second.pad2())
        .replace("s", second.toString())
        .replace("SSS", nanosecond.pad3())
        .replace("S", nanosecond.toString())
}

private fun Int.pad2(): String = toString().padStart(2, '0')
private fun Int.pad3(): String = toString().padStart(2, '0')