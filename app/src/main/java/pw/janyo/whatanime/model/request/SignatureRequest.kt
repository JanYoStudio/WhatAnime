package pw.janyo.whatanime.model.request

import vip.mystery0.tools.factory.mimeType
import java.io.File

data class SignatureRequest(val title: String,
							val fileSize: Long,
							val mimeType: String) {
	constructor(file: File) : this(file.name, file.length(), file.mimeType!!)
}