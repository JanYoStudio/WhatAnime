package pw.janyo.whatanime.model.request

data class SignatureRequest(val title: String,
							val fileSize: Long,
							val mimeType: String)