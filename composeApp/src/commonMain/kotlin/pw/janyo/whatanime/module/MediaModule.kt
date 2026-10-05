package pw.janyo.whatanime.module

import org.koin.dsl.module
import pw.janyo.whatanime.viewmodel.PlaybackCoordinator

val mediaModule = module {
    // 唯一资源所有者为 App 根部；Screen/ViewModel 不自行释放另一个页面的播放器。
    single { PlaybackCoordinator() }
}
