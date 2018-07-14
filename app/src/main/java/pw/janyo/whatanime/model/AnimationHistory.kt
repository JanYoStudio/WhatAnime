package pw.janyo.whatanime.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_animation_history")
class AnimationHistory {
	@PrimaryKey(autoGenerate = true)
	var id = 0

	@ColumnInfo(name = "origin_path")
	lateinit var originPath: String//原始路径
	@ColumnInfo(name = "cache_path")
	lateinit var cachePath: String//缓存路径
	@ColumnInfo(name = "animation_result")
	lateinit var result: String//结果
	@ColumnInfo(name = "animation_time")
	var time: Long = 0L//时间
	@ColumnInfo(name = "animation_title")
	lateinit var title: String//标题
	@ColumnInfo(name = "animation_filter")
	var filter: String? = null//过滤
}