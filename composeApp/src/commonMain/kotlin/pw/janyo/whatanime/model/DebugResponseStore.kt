package pw.janyo.whatanime.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 只接收已脱敏内容，原子更新避免并发响应突破上限或与 UI 读取竞争。 */
class DebugResponseStore {
    private val mutableEntries = MutableStateFlow<List<DebugHttpInfo>>(emptyList())
    val entries = mutableEntries.asStateFlow()
    fun append(entry: DebugHttpInfo) { mutableEntries.update { (it + entry).takeLast(5) } }
}
