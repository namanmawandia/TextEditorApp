package com.example.texteditor.ui.editor

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.ArrowKeyMovementMethod
import android.view.Gravity
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doOnTextChanged
import com.example.texteditor.ui.theme.RichTextEditorTheme

private class RichEditText(context: Context) : EditText(context) {
    var selectionChangeListener: ((Int, Int) -> Unit)? = null

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        selectionChangeListener?.invoke(selStart, selEnd)
    }
}

@Composable
fun RichTextEditor(
    content: SpannableStringBuilder,
    onContentChanged: (SpannableStringBuilder) -> Unit,
    onSelectionChanged: (SpannableStringBuilder, Int, Int) -> Unit,
    onEditTextReady: (EditText) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            RichEditText(context).apply {
                background = null
                setPadding(48, 48, 48, 48)
                textSize = 16f
                gravity = Gravity.TOP or Gravity.START
                setTextColor(textColor)
                setHintTextColor(hintColor)
                hint = "Start writing..."
                movementMethod = ArrowKeyMovementMethod.getInstance()
                isSingleLine = false
                customSelectionActionModeCallback = null

                setText(content)
                setSelection(content.length)

                doOnTextChanged { text, _, _, _ ->
                    val ssb = if (text is SpannableStringBuilder) text
                    else SpannableStringBuilder(text ?: "")
                    onContentChanged(ssb)
                }

                selectionChangeListener = { start, end ->
                    onSelectionChanged(
                        SpannableStringBuilder(editableText),
                        start,
                        end
                    )
                }
                onEditTextReady(this)  // hand reference up immediately after setup
            }
        },
        update = { editText ->
            if (editText.text.toString() != content.toString()) {
                editText.setText(content)
                editText.setSelection(content.length)
            }
        }
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Rich text editor — empty")
@Composable
private fun PreviewRichTextEditorEmpty() {
    RichTextEditorTheme {
        RichTextEditor(
            content = SpannableStringBuilder(""),
            onContentChanged = {},
            onSelectionChanged = { _, _, _ -> },
            onEditTextReady = {}
        )
    }
}

@Preview(showBackground = true, name = "Rich text editor — with content")
@Composable
private fun PreviewRichTextEditorWithContent() {
    RichTextEditorTheme {
        RichTextEditor(
            content = SpannableStringBuilder("The quick brown fox jumps over the lazy dog."),
            onContentChanged = {},
            onSelectionChanged = { _, _, _ -> },
            onEditTextReady = {}
        )
    }
}