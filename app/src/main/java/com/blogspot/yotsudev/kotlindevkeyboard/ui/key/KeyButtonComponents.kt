package com.blogspot.yotsudev.kotlindevkeyboard.ui.key

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blogspot.yotsudev.kotlindevkeyboard.data.LocalKeyboardSettings
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Simple tap-only key; no flick support
@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT,
    isSpecial: Boolean = false,
    isSymbol: Boolean = false,
    onClick: () -> Unit,
) {
    val haptic   = LocalHapticFeedback.current
    val settings = LocalKeyboardSettings.current
    val ap       = LocalKeyboardAppearance.current

    val brush: Brush = when {
        isSpecial -> ap.special
        isSymbol  -> ap.symbol
        else      -> ap.regular
    }
    val contentColor = if (isSymbol) ap.symbolContent else ap.keyContent

    Box(
        modifier = modifier
            .padding(2.dp)
            .shadow(KB_SHADOW_ELEV, KB_SHAPE)
            .height(height)
            .background(brush, KB_SHAPE)
            .border(KB_BORDER_WIDTH, ap.borderColor, KB_SHAPE)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    if (settings.hapticEnabled)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        up.consume()
                        onClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = label,
            color    = contentColor,
            fontSize = if (isSymbol) KEY_FONT_SIZE_SYMBOL else KEY_FONT_SIZE_NORMAL,
        )
    }
}

// Cycles OFF → ONCE → LOCK; accent brush when active
@Composable
fun ShiftKeyButton(
    shiftState: ShiftState,
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT,
    onClick: () -> Unit,
) {
    val haptic   = LocalHapticFeedback.current
    val settings = LocalKeyboardSettings.current
    val ap       = LocalKeyboardAppearance.current

    val brush: Brush = remember(shiftState, ap) {
        when (shiftState) {
            ShiftState.OFF  -> ap.special
            ShiftState.ONCE,
            ShiftState.LOCK -> KeyAccentBrush
        }
    }
    val label     = if (shiftState == ShiftState.LOCK) "⇪" else "⇧"
    val borderClr = if (shiftState == ShiftState.LOCK) Color(0x80FFFFFF) else ap.borderColor

    Box(
        modifier = modifier
            .padding(2.dp)
            .shadow(KB_SHADOW_ELEV, KB_SHAPE)
            .height(height)
            .background(brush, KB_SHAPE)
            .border(KB_BORDER_WIDTH, borderClr, KB_SHAPE)
            .semantics { role = Role.Button }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    if (settings.hapticEnabled)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val up = waitForUpOrCancellation()
                    if (up != null) {
                        up.consume()
                        onClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val textColor = when (shiftState) {
            ShiftState.OFF  -> ap.keyContent
            ShiftState.ONCE,
            ShiftState.LOCK -> Color.White
        }
        Text(
            text       = label,
            color      = textColor,
            fontSize   = KEY_FONT_SIZE_SHIFT,
            fontWeight = FontWeight.Bold,
        )
    }
}

// Repeats deletion with accelerating interval while held
@Composable
fun BackspaceKeyButton(
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT,
    onBackspace: () -> Unit,
) {
    val haptic    = LocalHapticFeedback.current
    val scope     = rememberCoroutineScope()
    val settings  = LocalKeyboardSettings.current
    var isPressed by remember { mutableStateOf(false) }
    val ap        = LocalKeyboardAppearance.current

    val brush = if (isPressed) ap.active else ap.special

    Box(
        modifier = modifier
            .padding(2.dp)
            .shadow(KB_SHADOW_ELEV, KB_SHAPE)
            .height(height)
            .background(brush, KB_SHAPE)
            .border(KB_BORDER_WIDTH, ap.borderColor, KB_SHAPE)
            .semantics { role = Role.Button }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    isPressed = true
                    val repeatJob = scope.launch {
                        onBackspace()
                        if (settings.hapticEnabled)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        delay(REPEAT_INITIAL_DELAY_MS)
                        var interval = REPEAT_INITIAL_INTERVAL_MS
                        while (true) {
                            onBackspace()
                            delay(interval)
                            interval = (interval - REPEAT_ACCEL_STEP)
                                .coerceAtLeast(REPEAT_MIN_INTERVAL_MS)
                        }
                    }
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.all { !it.pressed }) break
                        }
                    } finally {
                        repeatJob.cancel()
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "⌫",
            color      = ap.keyContent,
            fontSize   = KEY_FONT_SIZE_NORMAL,
            fontWeight = FontWeight.Medium,
        )
    }
}