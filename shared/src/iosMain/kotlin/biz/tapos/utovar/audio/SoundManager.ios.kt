package biz.tapos.utovar.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

actual class SoundManager {
    actual fun playSuccess() {
        // Kratak prijatan "bip" (System Sound 1057) + kratka vibracija
        AudioServicesPlaySystemSound(1057u)
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }

    actual fun playError() {
        // Upozoravajući "alarm" ton (System Sound 1073) + vibracija
        AudioServicesPlaySystemSound(1073u)
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }
}

@Composable
actual fun rememberSoundManager(): SoundManager {
    return remember { SoundManager() }
}