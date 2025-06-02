package pw.janyo.whatanime.utils

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import org.kotlincrypto.hash.md.MD5
import org.kotlincrypto.hash.sha1.SHA1
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.hash.sha2.SHA384
import org.kotlincrypto.hash.sha2.SHA512

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(): String = MD5().digest(encodeToByteArray()).toHexString()

@OptIn(ExperimentalStdlibApi::class)
fun String.sha1(): String = SHA1().digest(encodeToByteArray()).toHexString()

@OptIn(ExperimentalStdlibApi::class)
fun String.sha256(): String = SHA256().digest(encodeToByteArray()).toHexString()

@OptIn(ExperimentalStdlibApi::class)
fun String.sha512(): String = SHA512().digest(encodeToByteArray()).toHexString()

@OptIn(ExperimentalStdlibApi::class)
fun String.sha384(): String = SHA384().digest(encodeToByteArray()).toHexString()

@OptIn(ExperimentalStdlibApi::class)
suspend fun PlatformFile.md5(): String {
    val digest = MD5()
    digest.update(readBytes())
    val bytes = digest.digest()
    return bytes.toHexString()
}