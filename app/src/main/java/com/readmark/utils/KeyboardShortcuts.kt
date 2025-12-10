package com.readmark.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Constants for undo functionality
 */
private const val MAX_UNDO_STACK_SIZE = 20
private const val UNDO_DEBOUNCE_MS = 500L

/**
 * Data class to represent undo state
 */
data class UndoState(
    val textFieldValue: TextFieldValue,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Modifier extension to add keyboard shortcuts to TextField
 *
 * Supports:
 * - Ctrl+C: Copy selected text to clipboard
 * - Ctrl+V: Paste from clipboard
 * - Ctrl+X: Cut selected text to clipboard
 * - Ctrl+A: Select all text
 * - Ctrl+Z: Undo last change
 *
 * @param textFieldValueState The mutable state containing the TextFieldValue
 * @param clipboardManager The clipboard manager for copy/paste operations
 * @param undoStack The list containing undo history
 * @param undoIndex The current position in the undo stack
 * @param onUndoIndexChange Callback to update the undo index
 */
fun Modifier.keyboardShortcuts(
    textFieldValueState: MutableState<TextFieldValue>,
    clipboardManager: ClipboardManager,
    undoStack: SnapshotStateList<UndoState>,
    undoIndex: Int,
    onUndoIndexChange: (Int) -> Unit
): Modifier = this.onPreviewKeyEvent { keyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) {
        return@onPreviewKeyEvent false
    }

    val isCtrlPressed = keyEvent.isCtrlPressed || keyEvent.isMetaPressed

    if (!isCtrlPressed) {
        return@onPreviewKeyEvent false
    }

    when (keyEvent.key) {
        Key.C -> handleCopy(textFieldValueState.value, clipboardManager)
        Key.V -> handlePaste(textFieldValueState, clipboardManager, undoStack, onUndoIndexChange)
        Key.X -> handleCut(textFieldValueState, clipboardManager, undoStack, onUndoIndexChange)
        Key.A -> handleSelectAll(textFieldValueState)
        Key.Z -> handleUndo(textFieldValueState, undoStack, undoIndex, onUndoIndexChange)
        else -> false
    }
}

/**
 * Handle Ctrl+C (Copy)
 * Copies the selected text to the clipboard
 *
 * @return true if text was copied, false otherwise
 */
private fun handleCopy(
    textFieldValue: TextFieldValue,
    clipboardManager: ClipboardManager
): Boolean {
    val selectedText = textFieldValue.getSelectedText()
    if (selectedText.text.isNotEmpty()) {
        clipboardManager.setText(AnnotatedString(selectedText.text))
        return true
    }
    return false
}

/**
 * Handle Ctrl+V (Paste)
 * Pastes text from clipboard at the current cursor position or replaces selection
 *
 * @return true if text was pasted, false otherwise
 */
private fun handlePaste(
    textFieldValueState: MutableState<TextFieldValue>,
    clipboardManager: ClipboardManager,
    undoStack: SnapshotStateList<UndoState>,
    onUndoIndexChange: (Int) -> Unit
): Boolean {
    val clipText = clipboardManager.getText()?.text ?: ""
    if (clipText.isEmpty()) {
        return false
    }

    val currentValue = textFieldValueState.value

    // Save current state to undo stack before making changes
    pushToUndoStack(currentValue, undoStack, onUndoIndexChange)

    // Calculate new text by replacing the selection with pasted text
    val newText = currentValue.text.replaceRange(
        currentValue.selection.start,
        currentValue.selection.end,
        clipText
    )

    // Calculate new cursor position (after pasted text)
    val newCursorPosition = currentValue.selection.start + clipText.length

    // Update state
    textFieldValueState.value = TextFieldValue(
        text = newText,
        selection = TextRange(newCursorPosition)
    )

    return true
}

/**
 * Handle Ctrl+X (Cut)
 * Cuts the selected text to clipboard
 *
 * @return true if text was cut, false otherwise
 */
private fun handleCut(
    textFieldValueState: MutableState<TextFieldValue>,
    clipboardManager: ClipboardManager,
    undoStack: SnapshotStateList<UndoState>,
    onUndoIndexChange: (Int) -> Unit
): Boolean {
    val currentValue = textFieldValueState.value
    val selectedText = currentValue.getSelectedText()

    if (selectedText.text.isEmpty()) {
        return false
    }

    // Copy to clipboard
    clipboardManager.setText(AnnotatedString(selectedText.text))

    // Save current state to undo stack before making changes
    pushToUndoStack(currentValue, undoStack, onUndoIndexChange)

    // Remove selected text
    val newText = currentValue.text.removeRange(
        currentValue.selection.start,
        currentValue.selection.end
    )

    // Update state with cursor at the position where text was removed
    textFieldValueState.value = TextFieldValue(
        text = newText,
        selection = TextRange(currentValue.selection.start)
    )

    return true
}

/**
 * Handle Ctrl+A (Select All)
 * Selects all text in the field
 *
 * @return true always (event is consumed)
 */
private fun handleSelectAll(
    textFieldValueState: MutableState<TextFieldValue>
): Boolean {
    val currentValue = textFieldValueState.value
    textFieldValueState.value = currentValue.copy(
        selection = TextRange(0, currentValue.text.length)
    )
    return true
}

/**
 * Handle Ctrl+Z (Undo)
 * Restores the previous state from the undo stack
 *
 * @return true if undo was performed, false if stack is empty
 */
private fun handleUndo(
    textFieldValueState: MutableState<TextFieldValue>,
    undoStack: SnapshotStateList<UndoState>,
    undoIndex: Int,
    onUndoIndexChange: (Int) -> Unit
): Boolean {
    if (undoStack.isEmpty() || undoIndex <= 0) {
        return false
    }

    val newIndex = undoIndex - 1
    val previousState = undoStack[newIndex]

    textFieldValueState.value = previousState.textFieldValue
    onUndoIndexChange(newIndex)

    return true
}

/**
 * Push current state to undo stack
 * Limits stack size to MAX_UNDO_STACK_SIZE
 *
 * @param textFieldValue The current text field value to save
 * @param undoStack The undo stack to push to
 * @param onUndoIndexChange Callback to update the undo index
 */
fun pushToUndoStack(
    textFieldValue: TextFieldValue,
    undoStack: SnapshotStateList<UndoState>,
    onUndoIndexChange: (Int) -> Unit
) {
    // Remove any states after current undo index (for potential redo support)
    while (undoStack.size > 0 && undoStack.size > undoStack.lastIndex + 1) {
        undoStack.removeAt(undoStack.lastIndex)
    }

    // Add new state
    undoStack.add(UndoState(textFieldValue))

    // Limit stack size
    if (undoStack.size > MAX_UNDO_STACK_SIZE) {
        undoStack.removeAt(0)
    }

    // Update undo index to point to the new state
    onUndoIndexChange(undoStack.size)
}

/**
 * Extension function to get selected text from TextFieldValue
 *
 * @return AnnotatedString containing the selected text, or empty string if no selection
 */
private fun TextFieldValue.getSelectedText(): AnnotatedString {
    return if (selection.start != selection.end) {
        AnnotatedString(text.substring(selection.start, selection.end))
    } else {
        AnnotatedString("")
    }
}
