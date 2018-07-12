package pw.janyo.whatanime.model

class Animation {
	/**
	 * RawDocsCount : 6144137
	 * RawDocsSearchTime : 4593867992625
	 * ReRankSearchTime : 10719025293345
	 * CacheHit : false
	 * trial : 1
	 * quota : 8
	 * expire : 4
	 * docs : [{"from":1281.75,"to":1282.33,"anilist_id":19815,"at":1282.33,"season":"2014-04","anime":"No Game No Life","filename":"[Airota][No Game No Life][AudioCommentary][Vol.4][BDrip_720p][x264_AAC][CHT].mp4","episode":"","tokenthumb":"E39Qr_lq0qqa62vhihmPNQ","similarity":0.97,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1340.08,"to":1340.17,"anilist_id":19815,"at":1340.08,"season":"2014-04","anime":"No Game No Life","filename":"[KTXP][NO GAME NO LIFE][11][BIG5][720P].mp4","episode":11,"tokenthumb":"0dgA0MqrsTmUjz29NOmkbQ","similarity":0.97,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1340.08,"to":1340.17,"anilist_id":19815,"at":1340.17,"season":"2014-04","anime":"No Game No Life","filename":"[KTXP][NO GAME NO LIFE][10][BIG5][720P].mp4","episode":10,"tokenthumb":"XgHZ6WYLZ0uLDhKI4saxfQ","similarity":0.9685373563005802,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1340.17,"to":1340.58,"anilist_id":19815,"at":1340.17,"season":"2014-04","anime":"No Game No Life","filename":"[KTXP][NO GAME NO LIFE][03][BIG5][720P].mp4","episode":3,"tokenthumb":"XgHZ6WYLZ0uLDhKI4saxfQ","similarity":0.9685373563005802,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1340.08,"to":1340.17,"anilist_id":19815,"at":1340.08,"season":"2014-04","anime":"No Game No Life","filename":"[KTXP][NO GAME NO LIFE][06][BIG5][720P].mp4","episode":6,"tokenthumb":"0dgA0MqrsTmUjz29NOmkbQ","similarity":0.9658578643762691,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1282.5,"to":1282.58,"anilist_id":19815,"at":1282.58,"season":"2014-04","anime":"No Game No Life","filename":"[KTXP][NO GAME NO LIFE][08][BIG5][720P].mp4","episode":8,"tokenthumb":"MaBJgReo9XJKTCHSOkurMw","similarity":0.9658578643762691,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1340.17,"to":1340.42,"anilist_id":19815,"at":1340.17,"season":"2014-04","anime":"No Game No Life","filename":"[KTXP][NO GAME NO LIFE][02][BIG5][720P].mp4","episode":2,"tokenthumb":"XgHZ6WYLZ0uLDhKI4saxfQ","similarity":0.9658578643762691,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false},{"from":1339.92,"to":1340,"anilist_id":19815,"at":1339.92,"season":"2014-04","anime":"No Game No Life","filename":"[Airota][No Game No Life][AudioCommentary][Vol.3][BDrip_720p][x264_AAC][CHT].mp4","episode":"","tokenthumb":"zbCMWln82hAKlM3X5dnaCQ","similarity":0.9653589838486225,"title":"ノーゲーム・ノーライフ","title_native":"ノーゲーム・ノーライフ","title_chinese":"No Game No Life","title_english":"No Game No Life","title_romaji":"No Game No Life","mal_id":19815,"synonyms":[],"synonyms_chinese":["遊戲人生"],"is_adult":false}]
	 */

	var RawDocsCount: Int = 0
	var RawDocsSearchTime: Long = 0L
	var ReRankSearchTime: Long = 0L
	var CacheHit: Boolean = false//是否被缓存
	var trial: Int = 0//搜索次数
	var quota: Int = 0//剩余额度
	var expire: Int = 0//额度重置剩余时间
	var docs: ArrayList<Docs>? = null//结果
}
