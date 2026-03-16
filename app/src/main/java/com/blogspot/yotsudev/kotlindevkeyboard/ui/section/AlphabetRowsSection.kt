package com.blogspot.yotsudev.kotlindevkeyboard.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.blogspot.yotsudev.kotlindevkeyboard.data.KeyLabel
import com.blogspot.yotsudev.kotlindevkeyboard.data.alphaFlickRows
import com.blogspot.yotsudev.kotlindevkeyboard.data.applyShift
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.BackspaceKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.FlickKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.KeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.ShiftKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*

@Composable
fun AlphabetRowsSection(
    shiftState: ShiftState,
    onInput: (String) -> Unit,
    onShiftClick: () -> Unit,
    onBackspace: () -> Unit,
    onTab: () -> Unit,
) {
    alphaFlickRows.forEachIndexed { rowIdx, row ->
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            when (rowIdx) {

                0 -> row.forEach { key ->
                    FlickKeyButton(
                        flickKey = key.applyShift(shiftState),
                        modifier = Modifier.weight(KB_WEIGHT_NORMAL),
                        height   = KEY_HEIGHT,
                        onInput  = { onInput(it) },
                    )
                }

                1 -> {
                    KeyButton(
                        label     = KeyLabel.TAB,
                        modifier  = Modifier.weight(KB_WEIGHT_NORMAL),
                        height    = KEY_HEIGHT,
                        isSpecial = true,
                        onClick   = onTab,
                    )
                    row.forEach { key ->
                        FlickKeyButton(
                            flickKey = key.applyShift(shiftState),
                            modifier = Modifier.weight(KB_WEIGHT_NORMAL),
                            height   = KEY_HEIGHT,
                            onInput  = { onInput(it) },
                        )
                    }
                    Spacer(Modifier.weight(KB_WEIGHT_NORMAL))
                }

                2 -> {
                    ShiftKeyButton(
                        shiftState = shiftState,
                        modifier   = Modifier.weight(KB_WEIGHT_WIDE),
                        height     = KEY_HEIGHT,
                        onClick    = onShiftClick,
                    )
                    row.forEach { key ->
                        FlickKeyButton(
                            flickKey = key.applyShift(shiftState),
                            modifier = Modifier.weight(KB_WEIGHT_NORMAL),
                            height   = KEY_HEIGHT,
                            onInput  = { onInput(it) },
                        )
                    }
                    BackspaceKeyButton(
                        modifier    = Modifier.weight(KB_WEIGHT_WIDE),
                        height      = KEY_HEIGHT,
                        onBackspace = onBackspace,
                    )
                }
            }
        }
    }
}