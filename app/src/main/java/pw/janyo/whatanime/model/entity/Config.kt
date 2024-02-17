package pw.janyo.whatanime.model.entity

import android.os.Build

enum class NightMode(
    val value: Int,
    val title: String
) {
    AUTO(0, "自动"),
    ON(1, "始终开启"),
    OFF(2, "始终关闭"),
    MATERIAL_YOU(3, "Material You"),
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