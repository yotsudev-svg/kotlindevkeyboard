package com.blogspot.yotsudev.kotlindevkeyboard.ui.key

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.blogspot.yotsudev.kotlindevkeyboard.R
import com.blogspot.yotsudev.kotlindevkeyboard.data.LocalKeyboardSettings
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class FlickDirection { CENTER, UP, DOWN, LEFT, RIGHT }

data class FlickKey(
    val center: String,
    val up:        String? = null,
    val down:      String? = null,
    val left:      String? = null,
    val right:     String? = null,
    val longPress: String? = null,
)

@Composable
fun FlickKeyButton(
    flickKey: FlickKey,
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT,
    isSpecial: Boolean = false,
    isSymbol: Boolean = false,
    onInput: (String) -> Unit,
    onTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
) {
    val haptic   = LocalHapticFeedback.current
    val density  = LocalDensity.current
    val scope    = rememberCoroutineScope()
    val settings = LocalKeyboardSettings.current
    val ap       = LocalKeyboardAppearance.current

    val flickThresholdPx = with(density) { FLICK_THRESHOLD.toPx() }

    // Flick direction label strings for accessibility
    val flickUp    = stringResource(R.string.flick_direction_up)
    val flickDown  = stringResource(R.string.flick_direction_down)
    val flickLeft  = stringResource(R.string.flick_direction_left)
    val flickRight = stringResource(R.string.flick_direction_right)

    var direction   by remember { mutableStateOf(FlickDirection.CENTER) }
    var isDragging  by remember { mutableStateOf(false) }
    var longPressed by remember { mutableStateOf(false) }

    val baseBrush = when {
        isSpecial -> ap.special
        isSymbol  -> ap.symbol
        else      -> ap.regular
    }
    val currentBrush = if (isDragging) ap.active else baseBrush
    val contentColor = if (isSymbol) ap.symbolContent else ap.keyContent

    fun doHaptic() {
        if (settings.hapticEnabled)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun labelFor(dir: FlickDirection): String? = when (dir) {
        FlickDirection.CENTER -> flickKey.center
        FlickDirection.UP     -> flickKey.up
        FlickDirection.DOWN   -> flickKey.down
        FlickDirection.LEFT   -> flickKey.left
        FlickDirection.RIGHT  -> flickKey.right
    }

    // Describes all flick directions for screen readers
    val accessibilityDesc = buildString {
        append(flickKey.center)
        flickKey.up?.let    { append(flickUp.format(it)) }
        flickKey.down?.let  { append(flickDown.format(it)) }
        flickKey.left?.let  { append(flickLeft.format(it)) }
        flickKey.right?.let { append(flickRight.format(it)) }
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .shadow(KB_SHADOW_ELEV, KB_SHAPE)
            .height(height)
            .background(currentBrush, KB_SHAPE)
            .border(KB_BORDER_WIDTH, ap.borderColor, KB_SHAPE)
            .semantics {
                role = Role.Button
                contentDescription = accessibilityDesc
            }
            .pointerInput(flickKey) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    doHaptic()
                    isDragging  = false
                    longPressed = false
                    direction   = FlickDirection.CENTER

                    var totalOffset    = androidx.compose.ui.geometry.Offset.Zero
                    var flickTriggered = false

                    // Fires after hold delay if no flick started
                    val longPressJob = scope.launch {
                        delay(REPEAT_INITIAL_DELAY_MS)
                        if (!flickTriggered) {
                            val handled = when {
                                onLongPress != null        -> { onLongPress(); true }
                                flickKey.longPress != null -> { onInput(flickKey.longPress); true }
                                else -> false
                            }
                            if (handled) {
                                longPressed = true
                                doHaptic()
                            }
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val ptr   = event.changes.firstOrNull() ?: break

                        if (ptr.pressed) {
                            totalOffset += ptr.positionChange()
                            ptr.consume()

                            // Lock flick direction once threshold exceeded
                            val newDir = when {
                                abs(totalOffset.y) > abs(totalOffset.x) && totalOffset.y < -flickThresholdPx && flickKey.up    != null -> FlickDirection.UP
                                abs(totalOffset.y) > abs(totalOffset.x) && totalOffset.y >  flickThresholdPx && flickKey.down  != null -> FlickDirection.DOWN
                                abs(totalOffset.x) > abs(totalOffset.y) && totalOffset.x < -flickThresholdPx && flickKey.left  != null -> FlickDirection.LEFT
                                abs(totalOffset.x) > abs(totalOffset.y) && totalOffset.x >  flickThresholdPx && flickKey.right != null -> FlickDirection.RIGHT
                                else -> FlickDirection.CENTER
                            }

                            if (newDir != direction) {
                                direction  = newDir
                                isDragging = newDir != FlickDirection.CENTER
                                if (newDir != FlickDirection.CENTER) {
                                    flickTriggered = true
                                    longPressJob.cancel()
                                    doHaptic()
                                }
                            }
                        } else {
                            longPressJob.cancel()
                            if (!longPressed) {
                                val finalLabel = labelFor(direction)
                                if (direction == FlickDirection.CENTER && !flickTriggered) {
                                    if (onTap != null) onTap() else onInput(flickKey.center)
                                } else if (finalLabel != null) {
                                    onInput(finalLabel)
                                }
                            }
                            isDragging  = false
                            longPressed = false
                            direction   = FlickDirection.CENTER
                            break
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = flickKey.center,
            color      = contentColor,
            fontSize   = if (isSymbol) KEY_FONT_SIZE_SYMBOL else KEY_FONT_SIZE_NORMAL,
            fontWeight = FontWeight.Medium,
        )
        if (!isSymbol) {
            FlickSubLabels(flickKey = flickKey, contentColor = contentColor.copy(alpha = 0.4f))
        }
        // Popup appears above key while dragging
        if (isDragging && settings.popupEnabled) {
            Popup(
                alignment = Alignment.TopCenter,
                offset    = IntOffset(0, with(density) { (-height - POPUP_OFFSET_GAP).roundToPx() })
            ) {
                val popupShape = RoundedCornerShape(POPUP_CORNER_SIZE)
                Box(
                    modifier = Modifier
                        .size(POPUP_KEY_SIZE)
                        .shadow(6.dp, popupShape)
                        .background(KeyAccentBrush, popupShape)
                        .border(KB_BORDER_WIDTH, Color(0x40FFFFFF), popupShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = labelFor(direction) ?: flickKey.center,
                        color      = Color.White,
                        fontSize   = POPUP_FONT_SIZE,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// Corner sub-labels showing available flick targets
@Composable
fun FlickSubLabels(flickKey: FlickKey, contentColor: Color) {
    Box(modifier = Modifier.fillMaxSize().padding(2.dp)) {
        flickKey.up?.let {
            Text(it, color = contentColor, fontSize = KEY_FONT_SIZE_SUB,
                modifier = Modifier.align(Alignment.TopCenter))
        }
        flickKey.down?.let {
            Text(it, color = contentColor, fontSize = KEY_FONT_SIZE_SUB,
                modifier = Modifier.align(Alignment.BottomCenter))
        }
        flickKey.left?.let {
            Text(it, color = contentColor, fontSize = KEY_FONT_SIZE_SUB,
                modifier = Modifier.align(Alignment.CenterStart))
        }
        flickKey.right?.let {
            Text(it, color = contentColor, fontSize = KEY_FONT_SIZE_SUB,
                modifier = Modifier.align(Alignment.CenterEnd))
        }
        // Long-press label shown bold at top-right
        flickKey.longPress?.let {
            Text(
                text       = it,
                color      = contentColor.copy(alpha = 0.7f),
                fontSize   = KEY_FONT_SIZE_SUB,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}