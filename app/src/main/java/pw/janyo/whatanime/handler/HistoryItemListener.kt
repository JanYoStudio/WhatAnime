package pw.janyo.whatanime.handler

import android.content.Context
import pw.janyo.whatanime.databinding.ActivityHistoryBinding
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.activity.MainActivity
import java.io.File

class HistoryItemListener(private val context: Context,
						  private val activityHistoryBinding: ActivityHistoryBinding) {
	fun click(animationHistory: AnimationHistory, title: String) {
		MainActivity.showDetail(context, File(animationHistory.originPath), File(animationHistory.cachePath), title)
	}
}