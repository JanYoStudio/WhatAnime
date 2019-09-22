package pw.janyo.whatanime.model

class Animation {
	var RawDocsCount: Int = 0
	var RawDocsSearchTime: Long = 0L
	var ReRankSearchTime: Long = 0L
	var CacheHit: Boolean = false//是否被缓存
	var quota: Int = 0//剩余额度
	var quota_ttl: Int = 0//额度重置时间
	lateinit var docs: List<Docs>//结果
}
