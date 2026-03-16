package com.blogspot.yotsudev.kotlindevkeyboard.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.blogspot.yotsudev.kotlindevkeyboard.R
import com.blogspot.yotsudev.kotlindevkeyboard.data.*
import com.blogspot.yotsudev.kotlindevkeyboard.ui.theme.SettingsDimens

class DevKeyboardSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SettingsScreen() } }
    }
}

// Discriminated union for active dialog state
private sealed interface Dialog {
    data object CreateCategory : Dialog
    data class AddSnippet(val categoryId: String, val categoryName: String) : Dialog
    data class DeleteCategory(val category: UserCategory) : Dialog
    data class DeleteSnippet(val entry: CustomSnippetEntry) : Dialog
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var settings       by remember { mutableStateOf(KeyboardPreferences.load(context)) }
    var hiddenLabels   by remember { mutableStateOf(SnippetPreferences.loadHiddenLabels(context)) }
    var customEntries  by remember { mutableStateOf(SnippetPreferences.loadCustomEntries(context)) }
    var userCategories by remember { mutableStateOf(SnippetPreferences.loadUserCategories(context)) }
    var activeDialog   by remember { mutableStateOf<Dialog?>(null) }

    fun update(new: KeyboardSettings) {
        settings = new
        KeyboardPreferences.save(context, new)
    }

    fun toggleHidden(label: String, visible: Boolean) {
        hiddenLabels = if (visible) hiddenLabels - label else hiddenLabels + label
        SnippetPreferences.saveHiddenLabels(context, hiddenLabels)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SettingsDimens.screenPadding),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(SettingsDimens.sectionSpacing))

        Button(
            onClick  = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.settings_enable_ime)) }
        Spacer(Modifier.height(SettingsDimens.smallSpacing))
        Button(
            onClick  = {
                context.getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.settings_select_ime)) }

        Spacer(Modifier.height(SettingsDimens.sectionSpacing))
        HorizontalDivider()
        Spacer(Modifier.height(SettingsDimens.dividerSpacing))

        Text(stringResource(R.string.settings_section_theme), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(SettingsDimens.smallSpacing))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { i, mode ->
                SegmentedButton(
                    selected = settings.themeMode == mode,
                    onClick  = { update(settings.copy(themeMode = mode)) },
                    shape    = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                ) {
                    Text(
                        stringResource(
                            when (mode) {
                                ThemeMode.DARK   -> R.string.settings_theme_dark
                                ThemeMode.LIGHT  -> R.string.settings_theme_light
                                ThemeMode.SYSTEM -> R.string.settings_theme_system
                            }
                        )
                    )
                }
            }
        }
        // Dynamic color requires Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(Modifier.height(SettingsDimens.itemSpacing))
            SettingsToggleRow(
                title           = stringResource(R.string.settings_dynamic_color_title),
                description     = stringResource(R.string.settings_dynamic_color_desc),
                checked         = settings.dynamicColorEnabled,
                onCheckedChange = { update(settings.copy(dynamicColorEnabled = it)) }
            )
        }

        Spacer(Modifier.height(SettingsDimens.sectionSpacing))
        HorizontalDivider()
        Spacer(Modifier.height(SettingsDimens.dividerSpacing))

        Text(stringResource(R.string.settings_section_feedback), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(SettingsDimens.itemSpacing))
        SettingsToggleRow(
            title           = stringResource(R.string.settings_haptic_title),
            description     = stringResource(R.string.settings_haptic_desc),
            checked         = settings.hapticEnabled,
            onCheckedChange = { update(settings.copy(hapticEnabled = it)) }
        )
        Spacer(Modifier.height(SettingsDimens.smallSpacing))
        SettingsToggleRow(
            title           = stringResource(R.string.settings_popup_title),
            description     = stringResource(R.string.settings_popup_desc),
            checked         = settings.popupEnabled,
            onCheckedChange = { update(settings.copy(popupEnabled = it)) }
        )

        Spacer(Modifier.height(SettingsDimens.sectionSpacing))
        HorizontalDivider()
        Spacer(Modifier.height(SettingsDimens.dividerSpacing))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.settings_section_snippet), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { activeDialog = Dialog.CreateCategory }) {
                Text(stringResource(R.string.settings_snippet_create_category))
            }
        }
        Spacer(Modifier.height(SettingsDimens.microSpacing))
        Text(
            stringResource(R.string.settings_snippet_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(SettingsDimens.itemSpacing))

        // Built-in categories; CUSTOM rendered via user categories below
        SnippetCategory.entries
            .filter { it != SnippetCategory.CUSTOM }
            .forEach { category ->
                val defaultSnippets = kotlinSnippets.filter { it.category == category }
                val customInCat     = customEntries.filter { it.categoryId == category.name }
                var expanded by remember(category) { mutableStateOf(false) }

                val displayName = stringResource(category.displayNameRes)
                CategoryHeader(
                    name      = displayName,
                    expanded  = expanded,
                    canDelete = false,
                    onToggle  = { expanded = !expanded },
                    onAdd     = { activeDialog = Dialog.AddSnippet(category.name, displayName) },
                    onDelete  = {},
                )
                if (expanded) {
                    defaultSnippets.forEach { snippet ->
                        DefaultSnippetRow(
                            label     = snippet.label,
                            isVisible = snippet.label !in hiddenLabels,
                            onToggle  = { toggleHidden(snippet.label, it) },
                        )
                    }
                    customInCat.forEach { entry ->
                        CustomSnippetRow(
                            entry     = entry,
                            isVisible = entry.label !in hiddenLabels,
                            onToggle  = { toggleHidden(entry.label, it) },
                            onDelete  = { activeDialog = Dialog.DeleteSnippet(entry) },
                        )
                    }
                    Spacer(Modifier.height(SettingsDimens.tinySpacing))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

        Spacer(Modifier.height(SettingsDimens.tinySpacing))

        // User-created categories; deletable
        userCategories.forEach { cat ->
            val customInCat = customEntries.filter { it.categoryId == cat.id }
            var expanded by remember(cat.id) { mutableStateOf(false) }

            CategoryHeader(
                name      = cat.name,
                expanded  = expanded,
                canDelete = true,
                onToggle  = { expanded = !expanded },
                onAdd     = { activeDialog = Dialog.AddSnippet(cat.id, cat.name) },
                onDelete  = { activeDialog = Dialog.DeleteCategory(cat) },
            )
            if (expanded) {
                if (customInCat.isEmpty()) {
                    Text(
                        stringResource(R.string.settings_snippet_empty),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start  = SettingsDimens.snippetRowStartPadding,
                            top    = SettingsDimens.emptySnippetPaddingTop,
                            bottom = SettingsDimens.emptySnippetPaddingBottom,
                        ),
                    )
                } else {
                    customInCat.forEach { entry ->
                        CustomSnippetRow(
                            entry     = entry,
                            isVisible = entry.label !in hiddenLabels,
                            onToggle  = { toggleHidden(entry.label, it) },
                            onDelete  = { activeDialog = Dialog.DeleteSnippet(entry) },
                        )
                    }
                }
                Spacer(Modifier.height(SettingsDimens.tinySpacing))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }

    when (val d = activeDialog) {
        Dialog.CreateCategory -> CreateCategoryDialog(
            onConfirm = { name ->
                val newCat = UserCategory(id = "user_${System.currentTimeMillis()}", name = name)
                userCategories = userCategories + newCat
                SnippetPreferences.saveUserCategories(context, userCategories)
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )
        is Dialog.AddSnippet -> AddSnippetDialog(
            categoryName = d.categoryName,
            onConfirm    = { label, insert ->
                val entry = CustomSnippetEntry(label = label, insert = insert, categoryId = d.categoryId)
                customEntries = customEntries + entry
                SnippetPreferences.saveCustomEntries(context, customEntries)
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )
        is Dialog.DeleteCategory -> DeleteCategoryDialog(
            category  = d.category,
            onConfirm = {
                SnippetPreferences.deleteUserCategory(context, d.category.id)
                userCategories = SnippetPreferences.loadUserCategories(context)
                customEntries  = SnippetPreferences.loadCustomEntries(context)
                activeDialog   = null
            },
            onDismiss = { activeDialog = null }
        )
        is Dialog.DeleteSnippet -> DeleteSnippetDialog(
            entry     = d.entry,
            onConfirm = {
                customEntries = customEntries.filter { it !== d.entry }
                SnippetPreferences.saveCustomEntries(context, customEntries)
                activeDialog = null
            },
            onDismiss = { activeDialog = null }
        )
        null -> {}
    }
}

@Composable
private fun CategoryHeader(
    name: String, expanded: Boolean, canDelete: Boolean,
    onToggle: () -> Unit, onAdd: () -> Unit, onDelete: () -> Unit,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = SettingsDimens.categoryHeaderVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (canDelete) {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.settings_category_delete), color = MaterialTheme.colorScheme.error)
            }
        }
        TextButton(onClick = onAdd)    { Text(stringResource(R.string.settings_category_add)) }
        TextButton(onClick = onToggle) {
            Text(stringResource(if (expanded) R.string.settings_category_close else R.string.settings_category_edit))
        }
    }
}

@Composable
private fun DefaultSnippetRow(label: String, isVisible: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start  = SettingsDimens.snippetRowStartPadding,
                top    = SettingsDimens.snippetRowVerticalPadding,
                bottom = SettingsDimens.snippetRowVerticalPadding,
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = isVisible, onCheckedChange = onToggle)
    }
}

@Composable
private fun CustomSnippetRow(
    entry: CustomSnippetEntry,
    isVisible: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start  = SettingsDimens.snippetRowStartPadding,
                top    = SettingsDimens.snippetRowVerticalPadding,
                bottom = SettingsDimens.snippetRowVerticalPadding,
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(entry.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onDelete) {
            Text(
                stringResource(R.string.settings_category_delete),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(checked = isVisible, onCheckedChange = onToggle)
    }
}

@Composable
private fun CreateCategoryDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(stringResource(R.string.dialog_create_category_title)) },
        text   = {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text(stringResource(R.string.dialog_create_category_label)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name.trim()) }) {
                Text(stringResource(R.string.dialog_create_category_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }
    )
}

@Composable
private fun AddSnippetDialog(
    categoryName: String,
    onConfirm: (label: String, insert: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var label  by remember { mutableStateOf("") }
    var insert by remember { mutableStateOf("") }
    // Only ASCII labels are valid
    val isValid = label.isNotBlank() && label.all { it.code < 128 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(stringResource(R.string.dialog_add_snippet_title, categoryName)) },
        text   = {
            Column {
                OutlinedTextField(
                    value         = label,
                    onValueChange = { if (it.all { c -> c.code < 128 }) label = it },
                    label         = { Text(stringResource(R.string.dialog_add_snippet_label)) },
                    placeholder   = { Text(stringResource(R.string.dialog_add_snippet_placeholder)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    isError        = label.isNotEmpty() && !isValid,
                    supportingText = if (label.isNotEmpty() && !isValid) {
                        { Text(stringResource(R.string.dialog_add_snippet_error)) }
                    } else null,
                )
                Spacer(Modifier.height(SettingsDimens.dialogFieldSpacing))
                // Insert text defaults to label if left empty
                OutlinedTextField(
                    value         = insert,
                    onValueChange = { insert = it },
                    label         = { Text(stringResource(R.string.dialog_add_snippet_insert_label)) },
                    placeholder   = { Text(stringResource(R.string.dialog_add_snippet_insert_placeholder)) },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    maxLines      = 6,
                )
            }
        },
        confirmButton = {
            // insert falls back to label when left blank
            TextButton(enabled = isValid, onClick = { onConfirm(label.trim(), insert.ifBlank { label.trim() }) }) {
                Text(stringResource(R.string.dialog_add_snippet_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }
    )
}

@Composable
private fun DeleteCategoryDialog(category: UserCategory, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(stringResource(R.string.dialog_delete_category_title)) },
        text   = { Text(stringResource(R.string.dialog_delete_category_message, category.name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_confirm_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }
    )
}

@Composable
private fun DeleteSnippetDialog(entry: CustomSnippetEntry, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(stringResource(R.string.dialog_delete_snippet_title)) },
        text   = { Text(stringResource(R.string.dialog_delete_snippet_message, entry.label)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_confirm_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }
    )
}

@Composable
private fun SettingsToggleRow(
    title: String, description: String,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(SettingsDimens.toggleRowInnerSpacing))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}