package com.example.texteditor.ui.editor

import android.text.SpannableStringBuilder
import android.text.method.ArrowKeyMovementMethod
import android.view.Gravity
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doOnTextChanged
import com.example.texteditor.ui.theme.RichTextEditorTheme
import androidx.compose.material3.MaterialTheme


@Composable
fun RichTextEditor(
    content: SpannableStringBuilder,
    onContentChanged: (SpannableStringBuilder) -> Unit,
    onSelectionChanged: (SpannableStringBuilder, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            EditText(context).apply {
                background = null
                setPadding(48, 48, 48, 48)
                textSize = 16f
                gravity = Gravity.TOP or Gravity.START
                setTextColor(textColor)
                setHintTextColor(hintColor)
                hint = "Start writing..."
                movementMethod = ArrowKeyMovementMethod.getInstance()
                isSingleLine = false

                // Set initial content
                setText(content)
                setSelection(content.length)

                // Notify content changes upward
                doOnTextChanged { text, _, _, _ ->
                    if (text is SpannableStringBuilder) {
                        onContentChanged(text)
                    } else if (text != null) {
                        onContentChanged(SpannableStringBuilder(text))
                    }
                }

                // Notify selection changes so toolbar toggles update
                customSelectionActionModeCallback = null
                accessibilityDelegate
            }
        },
        update = { editText ->
            // Only update if content actually changed to avoid cursor jumping
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
            onSelectionChanged = { _, _, _ -> }
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
            onSelectionChanged = { _, _, _ -> }
        )
    }
}