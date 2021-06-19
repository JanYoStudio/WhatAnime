package pw.janyo.whatanime.handler

import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.activity.HistoryActivity
import pw.janyo.whatanime.ui.activity.MainActivity
import java.io.File

class HistoryItemListener(private val context: HistoryActivity) {
    fun click(animationHistory: AnimationHistory, title: String) {
        MainActivity.showDetail(
            context,
            File(animationHistory.cachePath),
            animationHistory.originPath,
            title
        )
    }
}