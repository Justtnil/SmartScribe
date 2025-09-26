package com.example.smartscribe.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    ERROR,
    SELECTION
}

@Composable
fun rememberHapticFeedback(): (HapticType) -> Unit {
    val context = LocalContext.current

    return remember@@androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE) { hapticType ->
        try {
            val contentResolver = context.contentResolver
            val isHapticFeedbackEnabled = Settings.System.getInt(
                contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            ) == 1

            if (!isHapticFeedbackEnabled) return@remember

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrator.hasVibrator()) { return@remember }

            val effect = when (hapticType) {
                HapticType.LIGHT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    } else {
                        VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                HapticType.MEDIUM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    } else {
                        VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                HapticType.HEAVY -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    } else {
                        VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                HapticType.SUCCESS -> {
                    VibrationEffect.createWaveform(
                        longArrayOf(30, 40, 30),
                        intArrayOf(255, 0, 255),
                        -1
                    )
                }
                HapticType.ERROR -> {
                    VibrationEffect.createOneShot(50, 200)
                }
                HapticType.SELECTION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    } else {
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 20),
                            intArrayOf(0, 180),
                            -1
                        )
                    }
                }
            }

            vibrator.cancel()
            vibrator.vibrate(effect)

        } catch (e: Exception) {
            // Ignore
        }
    }
}