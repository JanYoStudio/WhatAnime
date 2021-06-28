package pw.janyo.whatanime.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_animation_history")
class AnimationHistory {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    /**
     * 原始路径
     */
    @ColumnInfo(name = "origin_path")
    var originPath: String = ""

    /**
     * 缓存路径
     */
    @ColumnInfo(name = "cache_path")
    var cachePath: String = ""

    /**
     * 结果
     */
    @ColumnInfo(name = "animation_result")
    var result: String = ""

    /**
     * 时间
     */
    @ColumnInfo(name = "animation_time")
    var time: Long = 0L

    /**
     * 标题
     */
    @ColumnInfo(name = "animation_title")
    var title: String = ""

    /**
     * 过滤
     */
    @ColumnInfo(name = "animation_filter")
    var filter: String? = null
}