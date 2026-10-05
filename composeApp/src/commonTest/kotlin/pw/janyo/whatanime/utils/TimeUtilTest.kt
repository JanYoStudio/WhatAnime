package pw.janyo.whatanime.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUtilTest {
    private val english = mapOf(TimeUnit.MILLISECOND to "ms", TimeUnit.SECOND to "s", TimeUnit.MINUTE to "m", TimeUnit.HOUR to "h", TimeUnit.DAY to "d")

    @Test fun timeUnitsCanBeLocalizedWithoutChangingDecomposition() {
        assertEquals("1m3s", 63000L.formatTime(minTimeUnit = TimeUnit.SECOND, unitLabel = { english.getValue(it) }))
        assertEquals("1h1m3s", 3663000L.formatTime(minTimeUnit = TimeUnit.SECOND, unitLabel = { english.getValue(it) }))
    }

    @Test fun zeroAndMillisecondOnlyAlsoUseSuppliedLabels() {
        assertEquals("0s", 0L.formatTime(minTimeUnit = TimeUnit.SECOND, unitLabel = { english.getValue(it) }))
        assertEquals("123ms", 123L.formatTime(maxTimeUnit = TimeUnit.MILLISECOND, unitLabel = { english.getValue(it) }))
    }

    @Test fun relativeTimeUsesExactMinuteHourDayBoundaries() {
        assertEquals(RelativeTimeKind.JustNow, relativeTimeParts(0, 59999).kind)
        assertEquals(RelativeTimeParts(RelativeTimeKind.Minutes, 1), relativeTimeParts(0, 60000))
        assertEquals(RelativeTimeParts(RelativeTimeKind.Minutes, 59), relativeTimeParts(0, 3599999))
        assertEquals(RelativeTimeParts(RelativeTimeKind.Hours, 1), relativeTimeParts(0, 3600000))
        assertEquals(RelativeTimeParts(RelativeTimeKind.Days, 1), relativeTimeParts(0, 86400000))
    }

    @Test fun futureTimeCannotProduceNegativeRelativeLabels() {
        assertEquals(RelativeTimeParts(RelativeTimeKind.JustNow, 0), relativeTimeParts(1001, 1000))
    }

    @Test fun traditionalChineseUnitsAreNotReplacedWithSimplifiedDefaults() {
        assertEquals("1小時", 3600000L.formatTime(minTimeUnit = TimeUnit.SECOND, unitLabel = { if (it == TimeUnit.HOUR) "小時" else it.unit }))
    }
}
