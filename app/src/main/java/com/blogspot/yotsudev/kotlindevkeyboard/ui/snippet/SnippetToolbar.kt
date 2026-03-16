package com.blogspot.yotsudev.kotlindevkeyboard.ui.snippet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blogspot.yotsudev.kotlindevkeyboard.R
import com.blogspot.yotsudev.kotlindevkeyboard.data.Snippet
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.*

@Composable
fun SnippetToolbar(
    filterQuery: String,
    snippets: List<Snippet>,
    onSnippetSelect: (Snippet) -> Unit,
    onDismiss: () -> Unit,
) {
    // Re-filters only when query or snippet list changes
    val filtered = remember(filterQuery, snippets) {
        if (filterQuery.isEmpty()) snippets
        else snippets.filter { it.label.startsWith(filterQuery, ignoreCase = true) }
    }
    val ap = LocalKeyboardAppearance.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SYMBOL_KEY_HEIGHT)
            .padding(bottom = KB_ROW_BOTTOM_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(TOOLBAR_DISMISS_WIDTH)
                .fillMaxHeight()
                .padding(2.dp)
                .shadow(KB_SHADOW_ELEV, KB_SHAPE)
                .background(ap.special, KB_SHAPE)
                .border(KB_BORDER_WIDTH, ap.borderColor, KB_SHAPE)
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text     = stringResource(R.string.toolbar_dismiss),
                color    = ap.toolbarDismissIcon,
                fontSize = KEY_FONT_SIZE_SYMBOL,
            )
        }

        // Shows typed filter prefix as a chip
        if (filterQuery.isNotEmpty()) {
            val filterShape = RoundedCornerShape(TOOLBAR_FILTER_CORNER)
            Box(
                modifier = Modifier
                    .padding(horizontal = TOOLBAR_ITEM_SPACING)
                    .height(SYMBOL_KEY_HEIGHT - TOOLBAR_CHIP_OFFSET)
                    .background(ap.toolbarFilterBg, filterShape)
                    .border(KB_BORDER_WIDTH, ap.toolbarFilterBorder, filterShape)
                    .padding(horizontal = TOOLBAR_FILTER_H_PADDING),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = filterQuery,
                    color      = ap.toolbarFilterText,
                    fontSize   = KEY_FONT_SIZE_SMALL,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier         = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text     = stringResource(R.string.toolbar_no_candidates),
                    color    = ap.toolbarEmptyText,
                    fontSize = KEY_FONT_SIZE_SMALL,
                )
            }
        } else {
            // Announced to accessibility when list updates
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.spacedBy(TOOLBAR_ITEM_SPACING),
                contentPadding        = PaddingValues(horizontal = TOOLBAR_CONTENT_PADDING),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                items(filtered, key = { it.label }) { snippet ->
                    SnippetChip(snippet = snippet, onClick = { onSnippetSelect(snippet) })
                }
            }
        }
    }
}

@Composable
fun SnippetChip(snippet: Snippet, onClick: () -> Unit) {
    val haptic    = LocalHapticFeedback.current
    val ap        = LocalKeyboardAppearance.current
    val chipShape = RoundedCornerShape(TOOLBAR_CHIP_CORNER)
    val chipBrush = Brush.verticalGradient(listOf(ap.toolbarChipTopBg, ap.toolbarChipBottomBg))

    Box(
        modifier = Modifier
            .height(SYMBOL_KEY_HEIGHT - TOOLBAR_CHIP_OFFSET)
            .shadow(TOOLBAR_CHIP_SHADOW, chipShape)
            .background(chipBrush, chipShape)
            .border(KB_BORDER_WIDTH, ap.toolbarChipBorder, chipShape)
            .padding(horizontal = TOOLBAR_CHIP_H_PADDING)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = snippet.label,
            color      = ap.toolbarChipText,
            fontSize   = KEY_FONT_SIZE_SYMBOL,
            fontWeight = FontWeight.Medium,
            maxLines   = 1,
        )
    }
}