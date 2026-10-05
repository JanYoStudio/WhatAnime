package pw.janyo.whatanime.utils

fun isVideoExpired(savedAt: Long, now: Long): Boolean =
    savedAt <= Long.MAX_VALUE - 600_000L && now > savedAt + 600_000L

/** 只清理管理目录的直接子文件；传入的引用已在平台层统一恢复为当前缓存路径。 */
fun canDeleteCache(path: String, directory: String, referencedPaths: List<String>): Boolean {
    val root = directory.replace('\\', '/').trimEnd('/')
    val target = path.replace('\\', '/')
    if (target.substringBeforeLast('/', "") != root) return false
    if (target.substringAfterLast('/') in setOf("", ".", "..")) return false
    if (target.split('/').any { it == "." || it == ".." }) return false
    return referencedPaths.none { it.replace('\\', '/') == target }
}

/** 不解码或重编码已有 Token；仅替换 size 参数并保留 fragment。 */
fun previewUrl(url: String): String {
    val beforeFragment = url.substringBefore('#')
    val fragment = if ('#' in url) "#${url.substringAfter('#')}" else ""
    val base = beforeFragment.substringBefore('?')
    val query = beforeFragment.substringAfter('?', "").split('&')
        .filter { it.isNotEmpty() && it.substringBefore('=') != "size" }
    return "$base?${(query + "size=l").joinToString("&")}$fragment"
}
