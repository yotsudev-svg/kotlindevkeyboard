package com.blogspot.yotsudev.kotlindevkeyboard.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val KeyboardDarkColors = darkColorScheme(
    surface              = Color(0xFF0D0D0D),
    onSurface            = Color(0xFFE0E0E0),
    secondaryContainer   = Color(0xFF3A3A3A),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFF2C2C2C),
    onTertiaryContainer  = Color(0xFFB0C4FF),
)

val KeyboardLightColors = lightColorScheme(
    surface              = Color(0xFFF0F0F0),
    onSurface            = Color(0xFF1A1A1A),
    secondaryContainer   = Color(0xFFCCCCCC),
    onSecondaryContainer = Color(0xFF000000),
    tertiaryContainer    = Color(0xFFDDDDDD),
    onTertiaryContainer  = Color(0xFF1A3FAF),
)

// Shared by flick popup and active shift key
val KeyAccentTop    = Color(0xFF6B79D0)
val KeyAccentBottom = Color(0xFF5060C0)
val KeyAccentBrush  = Brush.verticalGradient(listOf(KeyAccentTop, KeyAccentBottom))

val KEY_HEIGHT        = 54.dp
val SYMBOL_KEY_HEIGHT = 48.dp

val KB_SHAPE        = RoundedCornerShape(10.dp)
val KB_SHADOW_ELEV  = 4.dp
val KB_BORDER_WIDTH = 0.5.dp

val KEY_FONT_SIZE_SUB    = 8.sp
val KEY_FONT_SIZE_SMALL  = 11.sp
val KEY_FONT_SIZE_SYMBOL = 12.sp
val KEY_FONT_SIZE_NORMAL = 13.sp
val KEY_FONT_SIZE_SHIFT  = 18.sp

val KB_ROW_BOTTOM_PADDING = 2.dp

// Row weight ratios for key width distribution
const val KB_WEIGHT_NORMAL = 1f
const val KB_WEIGHT_WIDE   = 1.5f  // Shift, Backspace, Enter
const val KB_WEIGHT_SPACE  = 3.5f

val POPUP_KEY_SIZE    = 52.dp
val POPUP_CORNER_SIZE = 10.dp
val POPUP_FONT_SIZE   = 22.sp
val POPUP_OFFSET_GAP  = 8.dp

// Distance thresholds for gesture recognition
val FLICK_THRESHOLD       = 20.dp
val SPACE_SWIPE_THRESHOLD = 10.dp

// Long-press and accelerating repeat timing
const val REPEAT_INITIAL_DELAY_MS    = 400L
const val REPEAT_INITIAL_INTERVAL_MS = 100L
const val REPEAT_MIN_INTERVAL_MS     = 30L
const val REPEAT_ACCEL_STEP          = 8L

// Bundles all key brushes, text colors, and toolbar colors per theme
data class KeyboardAppearance(
    val regular:       Brush,
    val special:       Brush,
    val symbol:        Brush,
    val active:        Brush,
    val keyContent:    Color,
    val symbolContent: Color,
    val borderColor:   Color,
    // Toolbar colors; themed so they adapt to dark/light mode
    val toolbarDismissIcon:  Color,
    val toolbarFilterBg:     Color,
    val toolbarFilterBorder: Color,
    val toolbarFilterText:   Color,
    val toolbarEmptyText:    Color,
    val toolbarChipTopBg:    Color,
    val toolbarChipBottomBg: Color,
    val toolbarChipBorder:   Color,
    val toolbarChipText:     Color,
)

val darkAppearance = KeyboardAppearance(
    regular       = Brush.verticalGradient(listOf(Color(0xFF2E2E2E), Color(0xFF1C1C1C))),
    special       = Brush.verticalGradient(listOf(Color(0xFF4A4A4A), Color(0xFF363636))),
    symbol        = Brush.verticalGradient(listOf(Color(0xFF3A3A3A), Color(0xFF272727))),
    active        = Brush.verticalGradient(listOf(Color(0xFF606060), Color(0xFF4A4A4A))),
    keyContent    = Color(0xFFE0E0E0),
    symbolContent = Color(0xFFB0C4FF),
    borderColor   = Color(0x28FFFFFF),
    toolbarDismissIcon  = Color(0xFFE0E0E0),
    toolbarFilterBg     = Color(0xFF1E2240),
    toolbarFilterBorder = Color(0x604060FF),
    toolbarFilterText   = Color(0xFF88AAFF),
    toolbarEmptyText    = Color(0xFF555555),
    toolbarChipTopBg    = Color(0xFF2E3D70),
    toolbarChipBottomBg = Color(0xFF1E2D58),
    toolbarChipBorder   = Color(0x404060FF),
    toolbarChipText     = Color(0xFFB0C4FF),
)

val lightAppearance = KeyboardAppearance(
    regular       = Brush.verticalGradient(listOf(Color(0xFFEBEBEB), Color(0xFFD8D8D8))),
    special       = Brush.verticalGradient(listOf(Color(0xFFCCCCCC), Color(0xFFBBBBBB))),
    symbol        = Brush.verticalGradient(listOf(Color(0xFFDFDFDF), Color(0xFFCFCFCF))),
    active        = Brush.verticalGradient(listOf(Color(0xFFB0B0B0), Color(0xFF949494))),
    keyContent    = Color(0xFF1A1A1A),
    symbolContent = Color(0xFF1A3FAF),
    borderColor   = Color(0x40000000),
    toolbarDismissIcon  = Color(0xFF1A1A1A),
    toolbarFilterBg     = Color(0xFFE3E8F8),
    toolbarFilterBorder = Color(0x601A3FAF),
    toolbarFilterText   = Color(0xFF1A3FAF),
    toolbarEmptyText    = Color(0xFF888888),
    toolbarChipTopBg    = Color(0xFFD4DCF5),
    toolbarChipBottomBg = Color(0xFFC4CCE8),
    toolbarChipBorder   = Color(0x401A3FAF),
    toolbarChipText     = Color(0xFF1A3FAF),
)

val LocalKeyboardAppearance = compositionLocalOf { darkAppearance }

val TOOLBAR_DISMISS_WIDTH    = 40.dp
// Chip height = SYMBOL_KEY_HEIGHT - TOOLBAR_CHIP_OFFSET
val TOOLBAR_CHIP_OFFSET      = 10.dp
val TOOLBAR_CHIP_CORNER      = 6.dp
val TOOLBAR_CHIP_SHADOW      = 2.dp
val TOOLBAR_CHIP_H_PADDING   = 10.dp
val TOOLBAR_FILTER_CORNER    = 4.dp
val TOOLBAR_FILTER_H_PADDING = 8.dp
val TOOLBAR_ITEM_SPACING     = 4.dp
val TOOLBAR_CONTENT_PADDING  = 4.dp

enum class ShiftState { OFF, ONCE, LOCK }