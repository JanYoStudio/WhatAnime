package pw.janyo.whatanime.ui.navigation

enum class TopLevelDestination { Search, History, Settings }
enum class BackAction { CloseOverlay, PopDetail, Search, System }

fun backAction(overlay: Boolean, detail: Boolean, destination: TopLevelDestination): BackAction = when {
    overlay -> BackAction.CloseOverlay
    detail -> BackAction.PopDetail
    destination != TopLevelDestination.Search -> BackAction.Search
    else -> BackAction.System
}
