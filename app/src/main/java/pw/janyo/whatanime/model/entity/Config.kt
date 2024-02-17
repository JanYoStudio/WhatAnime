package pw.janyo.whatanime.model.entity

import android.os.Build
import androidx.annotation.StringRes
import pw.janyo.whatanime.R

enum class NightMode(
    val value: Int,
    @StringRes
    val title: Int,
) {
    AUTO(0, R.string.array_night_mode_auto),
    ON(1, R.string.array_night_mode_always_on),
    OFF(2, R.string.array_night_mode_always_off),
    MATERIAL_YOU(3, R.string.array_night_mode_material_you),
    ;

    companion object {
        fun selectList(): List<NightMode> {
            val list = entries.sortedBy { it.value }.toMutableList()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                list.remove(MATERIAL_YOU)
            }
            return list
        }
    }
}