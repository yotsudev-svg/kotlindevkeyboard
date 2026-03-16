package com.blogspot.yotsudev.kotlindevkeyboard.data

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.core.content.edit

enum class ThemeMode { DARK, LIGHT, SYSTEM }

data class KeyboardSettings(
    val hapticEnabled: Boolean = true,
    val popupEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColorEnabled: Boolean = false,
)

val LocalKeyboardSettings = compositionLocalOf { KeyboardSettings() }

object KeyboardPreferences {
    private const val PREFS_NAME  = "keyboard_prefs"
    private const val KEY_HAPTIC  = "haptic_enabled"
    private const val KEY_POPUP   = "popup_enabled"
    private const val KEY_THEME   = "theme_mode"
    private const val KEY_DYNAMIC = "dynamic_color"

    fun load(context: Context): KeyboardSettings {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return KeyboardSettings(
            hapticEnabled       = p.getBoolean(KEY_HAPTIC, true),
            popupEnabled        = p.getBoolean(KEY_POPUP, true),
            themeMode           = ThemeMode.valueOf(
                p.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
            ),
            dynamicColorEnabled = p.getBoolean(KEY_DYNAMIC, false),
        )
    }

    fun save(context: Context, s: KeyboardSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_HAPTIC,  s.hapticEnabled)
            putBoolean(KEY_POPUP,   s.popupEnabled)
            putString(KEY_THEME,    s.themeMode.name)
            putBoolean(KEY_DYNAMIC, s.dynamicColorEnabled)
        }
    }
}