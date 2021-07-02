package pw.janyo.whatanime.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_animation_history")
class AnimationHistory {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "origin_path")
    var originPath: String = ""

    @ColumnInfo(name = "cache_path")
    var cachePath: String = ""

    @ColumnInfo(name = "animation_result")
    var result: String = ""

    @ColumnInfo(name = "animation_time")
    var time: Long = 0L

    @ColumnInfo(name = "animation_title")
    var title: String = ""

    @ColumnInfo(name = "animation_anilist_id")
    var anilistId: Long = 0L

    @ColumnInfo(name = "animation_episode")
    var episode: String = ""

    @ColumnInfo(name = "animation_similarity")
    var similarity: Double = 0.0
}