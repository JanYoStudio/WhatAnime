package pw.janyo.whatanime.classes

/**
 * Created by mystery0.
 */

class Dock {
	var from: Float = 0.toFloat()//开始
	var to: Float = 0.toFloat()//结束
	var at: Float = 0.toFloat()//位于
	var episode: String? = null
	var similarity: Float = 0.toFloat()//匹配度
	var anilist_id: String? = null//AniList id
	var mal_id: String? = null//MyAnimeList ID
	var title: String? = null//标题
	var title_native: String? = null//日语标题
	var title_chinese: String? = null//中文标题
	var title_english: String? = null//英文标题
	var title_romaji: String? = null//罗马字
	var synonyms: Array<String>? = null//备用英文标题
	var synonyms_chinese: Array<String>? = null//备用中文标题
	var season: String? = null//季度
	var anime: String? = null
	var filename: String? = null//匹配的文件名称
	var tokenthumb: String? = null//预览使用
}

