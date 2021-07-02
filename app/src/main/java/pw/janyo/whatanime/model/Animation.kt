package pw.janyo.whatanime.model

class Animation {
    var error: String = ""
    lateinit var result: List<Result>//结果
}

class Result {
    lateinit var anilist: Anilist
    var filename: String = ""
    var episode: String? = null
    var from: Double = 0.0//开始
    var to: Double = 0.0//结束
    var similarity: Double = 0.0//准确度
    var video: String = ""
    var image: String = ""
}

class Anilist {
    var id: Long = 0L
    var idMal: Long = 0L
    var title: Title? = null
    var synonyms: List<String>? = null
    var isAdult: Boolean = false
}

class Title {
    var native: String = ""
    var romaji: String = ""
    var english: String = ""
}