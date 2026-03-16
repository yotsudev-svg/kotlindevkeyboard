package com.blogspot.yotsudev.kotlindevkeyboard.ui.key

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blogspot.yotsudev.kotlindevkeyboard.R
import com.blogspot.yotsudev.kotlindevkeyboard.data.LocalKeyboardSettings
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class SwipeDirection { NONE, UP, DOWN, LEFT, RIGHT }

// Tap inserts space; swipe moves cursor with auto-repeat
@Composable
fun SpaceKeyButton(
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT,
    onSpace: () -> Unit,
    onCursorMove: (Int) -> Boolean,
) {
    val haptic   = LocalHapticFeedback.current
    val density  = LocalDensity.current
    val scope    = rememberCoroutineScope()
    val settings = LocalKeyboardSettings.current

    val lockThresholdPx      = with(density) { SPACE_SWIPE_THRESHOLD.toPx() }
    val spaceContentDesc     = stringResource(R.string.space_key_content_description)

    var swipeDir   by remember { mutableStateOf(SwipeDirection.NONE) }
    var isDragging by remember { mutableStateOf(false) }

    fun doHaptic() {
        if (settings.hapticEnabled)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val ap    = LocalKeyboardAppearance.current
    val brush = if (isDragging) ap.active else ap.special

    Box(
        modifier = modifier
            .padding(2.dp)
            .shadow(KB_SHADOW_ELEV, KB_SHAPE)
            .height(height)
            .background(brush, KB_SHAPE)
            .border(KB_BORDER_WIDTH, ap.borderColor, KB_SHAPE)
            .semantics {
                role = Role.Button
                contentDescription = spaceContentDesc
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    doHaptic()

                    var totalX         = 0f
                    var totalY         = 0f
                    var locked         = SwipeDirection.NONE
                    var flickTriggered = false
                    var repeatJob: kotlinx.coroutines.Job? = null

                    // Begins cursor-move repeat in the given direction
                    fun startRepeat(dir: SwipeDirection) {
                        val keyCode = when (dir) {
                            SwipeDirection.LEFT  -> KeyEvent.KEYCODE_DPAD_LEFT
                            SwipeDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
                            SwipeDirection.UP    -> KeyEvent.KEYCODE_DPAD_UP
                            SwipeDirection.DOWN  -> KeyEvent.KEYCODE_DPAD_DOWN
                            SwipeDirection.NONE  -> return
                        }
                        repeatJob = scope.launch {
                            val moved = onCursorMove(keyCode)
                            doHaptic()
                            if (!moved) return@launch

                            delay(REPEAT_INITIAL_DELAY_MS)
                            var interval = REPEAT_INITIAL_INTERVAL_MS
                            while (true) {
                                if (!onCursorMove(keyCode)) break
                                delay(interval)
                                interval = (interval - REPEAT_ACCEL_STEP)
                                    .coerceAtLeast(REPEAT_MIN_INTERVAL_MS)
                            }
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val ptr   = event.changes.firstOrNull() ?: break

                        if (ptr.pressed) {
                            val delta = ptr.positionChange()
                            ptr.consume()
                            totalX += delta.x
                            totalY += delta.y

                            // Axis is locked on first threshold crossing
                            if (locked == SwipeDirection.NONE) {
                                val newDir = when {
                                    abs(totalX) > abs(totalY) && abs(totalX) > lockThresholdPx ->
                                        if (totalX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                    abs(totalY) > abs(totalX) && abs(totalY) > lockThresholdPx ->
                                        if (totalY > 0) SwipeDirection.DOWN else SwipeDirection.UP
                                    else -> SwipeDirection.NONE
                                }
                                if (newDir != SwipeDirection.NONE) {
                                    locked         = newDir
                                    flickTriggered = true
                                    isDragging     = true
                                    swipeDir       = locked
                                    startRepeat(locked)
                                }
                            }
                        } else {
                            repeatJob?.cancel()
                            if (!flickTriggered) onSpace()
                            isDragging = false
                            swipeDir   = SwipeDirection.NONE
                            break
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Label reflects active swipe direction
        Text(
            text = when {
                !isDragging || swipeDir == SwipeDirection.NONE -> "Space"
                swipeDir == SwipeDirection.LEFT  -> "◀"
                swipeDir == SwipeDirection.RIGHT -> "▶"
                swipeDir == SwipeDirection.UP    -> "▲"
                else                             -> "▼"
            },
            color      = ap.keyContent,
            fontSize   = KEY_FONT_SIZE_NORMAL,
            fontWeight = FontWeight.Medium,
        )
    }
}