package pw.janyo.whatanime.model.request

import java.io.File

data class SignatureRequest(val title: String,
							val fileSize: Long,
							val mimeType: String) {
	constructor(file: File, mimeType: String) : this(file.name, file.length(), mimeType)
}