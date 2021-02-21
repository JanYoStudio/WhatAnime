package pw.janyo.whatanime.model.response

class SignatureResponse {
	lateinit var uploadUrl: String
	var uploadInternalUrl: String? = null
	var resourceId: Long = 0L
	lateinit var uploadMeta: UploadMeta
}

class UploadMeta {
	lateinit var key: String
	lateinit var signature: String
}