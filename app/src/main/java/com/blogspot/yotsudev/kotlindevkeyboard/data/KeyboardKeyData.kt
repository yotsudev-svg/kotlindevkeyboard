package com.blogspot.yotsudev.kotlindevkeyboard.data

import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.FlickKey
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.ShiftState

// Centralized key identifier constants
object KeyLabel {
    const val SPACE    = "Space"
    const val GLOBE    = "🌐"
    const val ENTER    = "↵"
    const val SNIPPET  = "SNP"
    const val ESC      = "ESC"
    const val PAREN    = "()"
    const val BRACE    = "{}"
    const val BRACKET  = "[]"
    const val BACKSPACE = "⌫"
    const val TAB      = "⇥"
}

// Symbol row: flick targets per key
val symbolFlickKeys = listOf(
    FlickKey(center = KeyLabel.ESC),
    FlickKey(center = KeyLabel.SNIPPET),
    FlickKey(center = "+",          left = "-",  up = "*",  right = "/",  down = "%"),
    FlickKey(center = KeyLabel.PAREN,   left = "(",  right = ")"),
    FlickKey(center = KeyLabel.BRACE,   left = "{",  right = "}"),
    FlickKey(center = KeyLabel.BRACKET, left = "[",  right = "]"),
    FlickKey(center = "$",          up = "!",    right = "|", left = "&", down = "#"),
    FlickKey(center = "\"",         longPress = "'"),
    FlickKey(center = ":",          longPress = ";"),
    FlickKey(center = "=",          up = "==",   down = "!="),
)

// QWERTY rows; long-press produces digits
val alphaFlickRows = listOf(
    listOf(
        FlickKey("q", longPress = "1"), FlickKey("w", longPress = "2"),
        FlickKey("e", longPress = "3"), FlickKey("r", longPress = "4"),
        FlickKey("t", longPress = "5"), FlickKey("y", longPress = "6"),
        FlickKey("u", longPress = "7"), FlickKey("i", longPress = "8"),
        FlickKey("o", longPress = "9"), FlickKey("p", longPress = "0"),
    ),
    listOf("a","s","d","f","g","h","j","k","l").map { FlickKey(it) },
    listOf("z","x","c","v","b","n","m").map { FlickKey(it) },
)

// Bottom row with punctuation flick targets
val bottomFlickKeys = listOf(
    FlickKey(center = KeyLabel.GLOBE),
    FlickKey(center = ",", left = "<", up = "_", down = "\\"),
    FlickKey(center = KeyLabel.SPACE),
    FlickKey(center = ".", right = ">", up = "?", down = "/"),
    FlickKey(center = KeyLabel.ENTER),
)

// Numeric keypad layout
val numericRows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", KeyLabel.BACKSPACE),
)

// Uppercases center label and flick targets when shift is active; longPress is excluded (digits)
fun FlickKey.applyShift(shiftState: ShiftState, includesLongPress: Boolean = false): FlickKey {
    if (shiftState == ShiftState.OFF) return this
    return copy(
        center    = center.uppercase(),
        up        = up?.uppercase(),
        down      = down?.uppercase(),
        left      = left?.uppercase(),
        right     = right?.uppercase(),
        longPress = if (includesLongPress) longPress?.uppercase() else longPress,
    )
}