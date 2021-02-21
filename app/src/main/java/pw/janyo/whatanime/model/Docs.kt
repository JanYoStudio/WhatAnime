package pw.janyo.whatanime.model

class Docs {
    val from: Double = 0.0//开始
    val to: Double = 0.0//结束
    val at: Double = 0.0//位于
    var episode: String? = null//集数
    val similarity: Double = 0.0//准确度
    val anilist_id: Int = 0//AniList id
    val mal_id: Int = 0//MyAnimeList id
    val is_adult: Boolean = false//十八禁
    var title_native: String? = null//日语标题
    var title_chinese: String? = null//中文标题
    var title_english: String? = null//英文标题
    var title_romaji: String? = null//罗马字
    var filename: String? = null//文件名
    var tokenthumb: String? = null//预览token
    var synonyms: List<String>? = null//备用英语标题
    var synonyms_chinese: List<String>? = null//备用中文标题
}
