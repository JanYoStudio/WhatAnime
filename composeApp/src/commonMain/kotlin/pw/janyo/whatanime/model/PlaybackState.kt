package pw.janyo.whatanime.model

enum class PlaybackPhase { Closed, Loading, Playing, Paused, Ended, Error }

data class PlaybackState(val sessionId: Long = 0, val phase: PlaybackPhase = PlaybackPhase.Closed)
