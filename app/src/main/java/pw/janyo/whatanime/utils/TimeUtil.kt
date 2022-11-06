package pw.janyo.whatanime.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        throw NumberFormatException("等级设置错误")
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
    val sb = StringBuffer()
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

fun Long.getCalendarFromLong(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar
}

fun Calendar.toDateTimeString(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(time)