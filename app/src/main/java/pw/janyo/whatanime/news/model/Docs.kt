package pw.janyo.whatanime.news.model

class Docs {
	/**
	 * from : 1281.75
	 * to : 1282.33
	 * anilist_id : 19815
	 * at : 1282.33
	 * season : 2014-04
	 * anime : No Game No Life
	 * filename : [Airota][No Game No Life][AudioCommentary][Vol.4][BDrip_720p][x264_AAC][CHT].mp4
	 * episode :
	 * tokenthumb : E39Qr_lq0qqa62vhihmPNQ
	 * similarity : 0.97
	 * title : ノーゲーム・ノーライフ
	 * title_native : ノーゲーム・ノーライフ
	 * title_chinese : No Game No Life
	 * title_english : No Game No Life
	 * title_romaji : No Game No Life
	 * mal_id : 19815
	 * synonyms : []
	 * synonyms_chinese : ["遊戲人生"]
	 * is_adult : false
	 */

	val from: Double = 0.0//开始
	val to: Double = 0.0//结束
	val at: Double = 0.0//位于
	val episode: String? = null//集数
	val similarity: Double = 0.0//准确度
	val anilist_id: Int = 0//AniList id
	val mal_id: Int = 0//MyAnimeList id
	val is_adult: Boolean = false//十八禁
	val title_native: String? = null//日语标题
	val title_chinese: String? = null//中文标题
	val title_english: String? = null//英文标题
	val title_romaji: String? = null//罗马字
	val filename: String? = null//文件名
	val tokenthumb: String? = null//预览token
	val synonyms: List<String>? = null//备用英语标题
	val synonyms_chinese: List<String>? = null//备用中文标题
}
