package biz.tapos.utovar.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class SoundManager(private val context: Context) {
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    actual fun playSuccess() {
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    actual fun playError() {
        toneGen.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 350)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}

@Composable
actual fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current
    return remember { SoundManager(context) }
}