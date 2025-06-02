package pw.janyo.whatanime.ui.theme

import android.os.Build

actual fun showNightModeSelectList(): List<NightMode> {
    val list = NightMode.entries.sortedBy { it.value }.toMutableList()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        list.remove(NightMode.MATERIAL_YOU)
    }
    return list
}