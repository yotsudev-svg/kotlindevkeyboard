package com.blogspot.yotsudev.kotlindevkeyboard.ui.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.blogspot.yotsudev.kotlindevkeyboard.data.KeyLabel
import com.blogspot.yotsudev.kotlindevkeyboard.data.numericRows
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.BackspaceKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.KeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.KEY_HEIGHT

@Composable
fun NumericLayout(onInput: (String) -> Unit, onBackspace: () -> Unit) {
    numericRows.forEach { row ->
        Row(modifier = Modifier.fillMaxWidth()) {
            row.forEach { label ->
                if (label == KeyLabel.BACKSPACE) {
                    BackspaceKeyButton(
                        modifier    = Modifier.weight(1f),
                        height      = KEY_HEIGHT,
                        onBackspace = onBackspace,
                    )
                } else {
                    KeyButton(
                        label    = label,
                        modifier = Modifier.weight(1f),
                        height   = KEY_HEIGHT,
                        isSymbol = true,
                        onClick  = { onInput(label) },
                    )
                }
            }
        }
    }
}