package com.blogspot.yotsudev.kotlindevkeyboard.ui.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.blogspot.yotsudev.kotlindevkeyboard.data.*
import com.blogspot.yotsudev.kotlindevkeyboard.ui.section.*
import com.blogspot.yotsudev.kotlindevkeyboard.ui.snippet.SnippetToolbar
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*

@Composable
fun KeyboardScreen(
    isPassword: Boolean,
    isNumeric: Boolean,
    settings: KeyboardSettings,
    snippets: List<Snippet>,
    onTextInput: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onEsc: () -> Unit,
    onSwitchIme: () -> Unit,
    onCursorMove: (Int) -> Boolean,
    onMoveCursorLeft: () -> Unit,
    onTab: () -> Unit,
    onSnippetSelect: (deleteCount: Int, insertText: String) -> Unit,
) {
    val context    = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val isDark = when (settings.themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

    // Dynamic color requires Android 12+
    val colorScheme = if (settings.dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) KeyboardDarkColors else KeyboardLightColors
    }

    val appearance = if (isDark) darkAppearance else lightAppearance

    var shiftState    by remember { mutableStateOf(ShiftState.OFF) }
    var isToolbarMode by remember { mutableStateOf(false) }
    var filterQuery   by remember { mutableStateOf("") }
    var isNumpadMode  by remember { mutableStateOf(false) }

    val showNumpad = isNumeric || isNumpadMode

    // Commits text to the editor; also updates filterQuery in toolbar mode for snippet filtering.
    // Typing continues normally and a snippet can optionally be selected to replace the prefix.
    fun input(text: String) {
        val actual = when (shiftState) {
            ShiftState.OFF  -> text
            ShiftState.ONCE -> { shiftState = ShiftState.OFF; text.uppercase() }
            ShiftState.LOCK -> text.uppercase()
        }
        if (isToolbarMode) filterQuery += actual
        onTextInput(actual)
    }

    // Bypasses input(): pairs are shift-invariant and must not accumulate into filterQuery.
    fun inputPair(open: String, close: String) {
        onTextInput("$open$close")
        onMoveCursorLeft()
    }

    // Keeps filterQuery in sync with the editor by trimming one char in toolbar mode.
    val handleBackspace: () -> Unit = {
        if (isToolbarMode && filterQuery.isNotEmpty()) filterQuery = filterQuery.dropLast(1)
        onBackspace()
    }

    // Replaces the typed prefix already in the editor with the chosen snippet.
    fun selectSnippet(snippet: Snippet) {
        onSnippetSelect(filterQuery.length, snippet.insert)
        isToolbarMode = false
        filterQuery   = ""
    }

    CompositionLocalProvider(
        LocalKeyboardSettings   provides settings,
        LocalKeyboardAppearance provides appearance,
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 2.dp, vertical = 4.dp)
            ) {
                if (showNumpad) {
                    NumericLayout(onInput = { input(it) }, onBackspace = handleBackspace)
                } else {
                    // Toolbar replaces symbol row during snippet search
                    if (isToolbarMode) {
                        SnippetToolbar(
                            filterQuery     = filterQuery,
                            snippets        = snippets,
                            onSnippetSelect = { selectSnippet(it) },
                            onDismiss       = {
                                // Text is already in the editor; just close the toolbar
                                isToolbarMode = false
                                filterQuery   = ""
                            }
                        )
                    } else {
                        SymbolRowSection(
                            onInput        = { input(it) },
                            onInputPair    = { o, c -> inputPair(o, c) },
                            onSpecialClick = { if (!isPassword) isToolbarMode = true },
                            onEsc          = onEsc,
                        )
                    }
                    AlphabetRowsSection(
                        shiftState   = shiftState,
                        onInput      = { input(it) },
                        onShiftClick = {
                            shiftState = when (shiftState) {
                                ShiftState.OFF  -> ShiftState.ONCE
                                ShiftState.ONCE -> ShiftState.LOCK
                                ShiftState.LOCK -> ShiftState.OFF
                            }
                        },
                        onBackspace  = handleBackspace,
                        onTab        = onTab
                    )
                }
                BottomRowSection(
                    isNumpadMode   = isNumpadMode,
                    onInput        = { input(it) },
                    onSwitchIme    = onSwitchIme,
                    onToggleNumpad = { if (!isNumeric && !isPassword) isNumpadMode = !isNumpadMode },
                    onEnter        = onEnter,
                    onCursorMove   = onCursorMove
                )
            }
        }
    }
}