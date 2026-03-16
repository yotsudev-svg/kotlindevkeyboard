package com.blogspot.yotsudev.kotlindevkeyboard.ui.section

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.blogspot.yotsudev.kotlindevkeyboard.data.KeyLabel
import com.blogspot.yotsudev.kotlindevkeyboard.data.bottomFlickKeys
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.FlickKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.SpaceKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*

@Composable
fun BottomRowSection(
    isNumpadMode: Boolean,
    onInput: (String) -> Unit,
    onSwitchIme: () -> Unit,
    onToggleNumpad: () -> Unit,
    onEnter: () -> Unit,
    onCursorMove: (Int) -> Boolean,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(top = KB_ROW_BOTTOM_PADDING),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        bottomFlickKeys.forEach { key ->
            when (key.center) {
                KeyLabel.SPACE -> SpaceKeyButton(
                    modifier     = Modifier.weight(KB_WEIGHT_SPACE),
                    height       = KEY_HEIGHT,
                    onSpace      = { onInput(" ") },
                    onCursorMove = onCursorMove,
                )
                // Tap switches IME; long-press toggles numpad
                KeyLabel.GLOBE -> FlickKeyButton(
                    flickKey    = key.copy(
                        longPress = if (isNumpadMode) "ABC" else "123",
                    ),
                    modifier    = Modifier.weight(KB_WEIGHT_NORMAL),
                    height      = KEY_HEIGHT,
                    isSpecial   = true,
                    onInput     = {},
                    onTap       = { onSwitchIme() },
                    onLongPress = { onToggleNumpad() },
                )
                KeyLabel.ENTER -> FlickKeyButton(
                    flickKey  = key,
                    modifier  = Modifier.weight(KB_WEIGHT_WIDE),
                    height    = KEY_HEIGHT,
                    isSpecial = true,
                    onInput   = { onInput(it) },
                    onTap     = { onEnter() },
                )
                else -> FlickKeyButton(
                    flickKey = key,
                    modifier = Modifier.weight(KB_WEIGHT_NORMAL),
                    height   = KEY_HEIGHT,
                    onInput  = { onInput(it) },
                )
            }
        }
    }
}