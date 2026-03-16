package com.blogspot.yotsudev.kotlindevkeyboard.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.blogspot.yotsudev.kotlindevkeyboard.data.KeyLabel
import com.blogspot.yotsudev.kotlindevkeyboard.data.symbolFlickKeys
import com.blogspot.yotsudev.kotlindevkeyboard.ui.key.FlickKeyButton
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*

@Composable
fun SymbolRowSection(
    onInput: (String) -> Unit,
    onInputPair: (String, String) -> Unit,
    onSpecialClick: () -> Unit,
    onEsc: () -> Unit,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(bottom = KB_ROW_BOTTOM_PADDING),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        symbolFlickKeys.forEach { key ->
            FlickKeyButton(
                flickKey  = key,
                modifier  = Modifier.weight(KB_WEIGHT_NORMAL),
                height    = SYMBOL_KEY_HEIGHT,
                isSymbol  = key.center != KeyLabel.ESC,
                isSpecial = key.center == KeyLabel.ESC,
                onInput   = { text ->
                    // Bracket keys insert pairs via onInputPair
                    when (text) {
                        KeyLabel.PAREN   -> onInputPair("(", ")")
                        KeyLabel.BRACE   -> onInputPair("{", "}")
                        KeyLabel.BRACKET -> onInputPair("[", "]")
                        else             -> onInput(text)
                    }
                },
                onTap = when (key.center) {
                    KeyLabel.ESC     -> { { onEsc() } }
                    KeyLabel.SNIPPET -> { { onSpecialClick() } }
                    KeyLabel.PAREN   -> { { onInputPair("(", ")") } }
                    KeyLabel.BRACE   -> { { onInputPair("{", "}") } }
                    KeyLabel.BRACKET -> { { onInputPair("[", "]") } }
                    else             -> null
                },
            )
        }
    }
}