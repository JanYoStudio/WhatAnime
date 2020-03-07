package pw.janyo.whatanime.model.request

import pw.janyo.whatanime.config.publicDeviceId
import java.io.File

data class SignatureRequest(val title: String,
							val fileSize: Long,
							val mimeType: String,
							val deviceId: String = publicDeviceId) {
	constructor(file: File, mimeType: String) : this(file.name, file.length(), mimeType)
}