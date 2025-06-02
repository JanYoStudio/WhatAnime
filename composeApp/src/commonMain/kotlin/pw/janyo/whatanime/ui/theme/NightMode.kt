package pw.janyo.whatanime.ui.theme

import org.jetbrains.compose.resources.StringResource
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.array_night_mode_always_off
import whatanime.composeapp.generated.resources.array_night_mode_always_on
import whatanime.composeapp.generated.resources.array_night_mode_auto
import whatanime.composeapp.generated.resources.array_night_mode_material_you

enum class NightMode(
    val value: Int,
    val title: StringResource,
) {
    AUTO(0, Res.string.array_night_mode_auto),
    ON(1, Res.string.array_night_mode_always_on),
    OFF(2, Res.string.array_night_mode_always_off),
    MATERIAL_YOU(3, Res.string.array_night_mode_material_you),
}

expect fun showNightModeSelectList(): List<NightMode>