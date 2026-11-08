package biz.tapos.utovar.audio

import androidx.compose.runtime.Composable

expect class SoundManager {
    fun playSuccess()
    fun playError()
}

@Composable
expect fun rememberSoundManager(): SoundManager