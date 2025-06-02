package pw.janyo.whatanime.utils

import kotlin.math.pow
import kotlin.math.roundToLong

fun formatDecimal(value: Double, digits: Int = 1): String {
    val multiplier = 10.0.pow(digits + 1)
    val rounded = (value * multiplier).roundToLong() / multiplier
    return rounded.toString()
}