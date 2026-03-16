package com.blogspot.yotsudev.kotlindevkeyboard.ime


import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.blogspot.yotsudev.kotlindevkeyboard.data.KeyboardPreferences
import com.blogspot.yotsudev.kotlindevkeyboard.data.KeyboardSettings
import com.blogspot.yotsudev.kotlindevkeyboard.data.Snippet
import com.blogspot.yotsudev.kotlindevkeyboard.data.SnippetPreferences
import com.blogspot.yotsudev.kotlindevkeyboard.ui.screen.KeyboardScreen
import kotlinx.coroutines.*

// Implements Lifecycle/ViewModelStore/SavedState for Compose inside an IME
class DevKeyboardService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override val viewModelStore = ViewModelStore()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Tied to UI thread dispatcher; canceled in onDestroy
    private val serviceScope = CoroutineScope(SupervisorJob() + AndroidUiDispatcher.CurrentThread)

    // Holds the active Recomposer so it can be canceled before a new one is created,
    // preventing leaks when onCreateInputView() is called multiple times by the system.
    private var activeRecomposer: Recomposer? = null

    private var isPasswordMode   = mutableStateOf(false)
    private var isNumericMode    = mutableStateOf(false)
    private var keyboardSettings = mutableStateOf(KeyboardSettings())
    private var visibleSnippets  = mutableStateOf<List<Snippet>>(emptyList())

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        keyboardSettings.value  = KeyboardPreferences.load(this)
        visibleSnippets.value   = SnippetPreferences.loadVisibleSnippets(this)
        serviceScope.launch(Dispatchers.IO) { preloadResources() }
    }

    // TODO: Preload heavy resources (e.g. custom fonts, assets) here before the view is shown
    private fun preloadResources() {}

    override fun onCreateInputView(): View {
        // Cancel the previous Recomposer before creating a new one to prevent leaks
        activeRecomposer?.cancel()

        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            // Manual Recomposer required outside Activity/Fragment
            val recomposer = Recomposer(AndroidUiDispatcher.CurrentThread)
            activeRecomposer = recomposer
            compositionContext = recomposer
            serviceScope.launch { recomposer.runRecomposeAndApplyChanges() }

            setViewTreeLifecycleOwner(this@DevKeyboardService)
            setViewTreeViewModelStoreOwner(this@DevKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@DevKeyboardService)

            setContent {
                KeyboardScreen(
                    isPassword       = isPasswordMode.value,
                    isNumeric        = isNumericMode.value,
                    settings         = keyboardSettings.value,
                    snippets         = visibleSnippets.value,
                    onTextInput      = { text -> sendText(text) },
                    onBackspace      = { sendBackspace() },
                    onEnter          = { sendEnter() },
                    onEsc            = { sendEsc() },
                    onSwitchIme      = { switchToNextInputMethod(false) },
                    onCursorMove     = { keyCode -> moveCursor(keyCode) },
                    onMoveCursorLeft = { moveCursorLeft() },
                    onTab            = { sendTab() },
                    onSnippetSelect  = { deleteCount, insertText ->
                        selectSnippet(deleteCount, insertText)
                    },
                )
            }
        }
    }

    // Reload settings and detect numeric/password field types
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        keyboardSettings.value = KeyboardPreferences.load(this)
        visibleSnippets.value  = SnippetPreferences.loadVisibleSnippets(this)

        info?.let {
            val inputClass = it.inputType and InputType.TYPE_MASK_CLASS
            isNumericMode.value = (inputClass == InputType.TYPE_CLASS_NUMBER ||
                    inputClass == InputType.TYPE_CLASS_PHONE)
            val variation = it.inputType and InputType.TYPE_MASK_VARIATION
            isPasswordMode.value = (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                    variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        activeRecomposer?.cancel()
        serviceScope.cancel()
        viewModelStore.clear()
        super.onDestroy()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    private fun sendText(text: String) { currentInputConnection?.commitText(text, 1) }
    private fun moveCursorLeft() { sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT) }
    private fun sendBackspace() { sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL) }

    // Sends ESC as a key event instead of committing "\u001B" as text
    private fun sendEsc() { sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE) }

    // Performs IME action when available; falls back to newline
    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val ei = currentInputEditorInfo ?: return
        val imeAction     = ei.imeOptions and EditorInfo.IME_MASK_ACTION
        val noEnterAction = (ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (imeAction != EditorInfo.IME_ACTION_NONE &&
            imeAction != EditorInfo.IME_ACTION_UNSPECIFIED &&
            !noEnterAction
        ) ic.performEditorAction(imeAction)
        else ic.commitText("\n", 1)
    }

    // Commits tab character; key event as fallback
    private fun sendTab() {
        val committed = currentInputConnection?.commitText("\t", 1) ?: false
        if (!committed) sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB)
    }

    // Replaces typed filter query with chosen snippet text
    private fun selectSnippet(deleteCount: Int, insertText: String) {
        val ic = currentInputConnection ?: return
        ic.beginBatchEdit()
        if (deleteCount > 0) ic.deleteSurroundingText(deleteCount, 0)
        ic.commitText(insertText, 1)
        ic.endBatchEdit()
    }

    // Returns false when cursor is already at the boundary
    private fun moveCursor(direction: Int): Boolean {
        val ic = currentInputConnection ?: return false
        val canMove = when (direction) {
            KeyEvent.KEYCODE_DPAD_LEFT  -> ic.getTextBeforeCursor(1, 0)?.isNotEmpty() == true
            KeyEvent.KEYCODE_DPAD_RIGHT -> ic.getTextAfterCursor(1, 0)?.isNotEmpty() == true
            else -> true  // UP/DOWN: no boundary detection available via InputConnection; key event sent regardless
        }
        if (!canMove) return false
        ic.beginBatchEdit()
        sendDownUpKeyEvents(direction)
        ic.endBatchEdit()
        return true
    }
}