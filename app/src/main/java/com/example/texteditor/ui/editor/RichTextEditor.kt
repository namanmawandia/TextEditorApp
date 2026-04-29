package com.example.texteditor.ui.editor

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.ArrowKeyMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
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
    var lastContent: Spannable = SpannableStringBuilder("")

    // Formatting state for "typing mode" (applied to next typed chars)
    var typingBold: Boolean = false
    var typingItalic: Boolean = false
    var typingUnderline: Boolean = false
    var typingStrikethrough: Boolean = false
    var typingTextColor: Int? = null
    var typingHighlightColor: Int? = null

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
    modifier: Modifier = Modifier,
    typingBold: Boolean,
    typingItalic: Boolean,
    typingUnderline: Boolean = false,
    typingStrikethrough: Boolean = false,
    typingTextColor: Int? = null,
    typingHighlightColor: Int? = null
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

                setText(content)
                setSelection(content.length)

                doOnTextChanged { text, start, before, count ->
                    val oldContent = (this as? RichEditText)?.lastContent ?: content
                    if (count > 0 && text is SpannableStringBuilder) {
                        val end = start + count
                        val beforeRangeEnd = start + before

                        /**
                         * Surgical formatting logic:
                         * 1. Clears current spans in the modified range [start, end).
                         * 2. RESTORES original spans for the 'before' part [start, start + before) 
                         *    from oldContent (to fix predictive text stripping spans).
                         * 3. APPLIES current toggle state to the 'new' characters [start + before, end).
                         */
                        fun <T : Any> restoreAndApply(
                            isToggledOn: Boolean,
                            spanClass: Class<T>,
                            createSpan: (T?) -> T,
                            filter: (T) -> Boolean = { true }
                        ) {
                            // 1. Clear
                            text.getSpans(start, end, spanClass).filter(filter).forEach { text.removeSpan(it) }

                            // 2. Restore (for characters that were already there)
                            if (before > 0) {
                                val oldSpans = oldContent.getSpans(start, beforeRangeEnd, spanClass).filter(filter)
                                for (span in oldSpans) {
                                    val s = oldContent.getSpanStart(span).coerceAtLeast(start)
                                    val e = oldContent.getSpanEnd(span).coerceAtMost(beforeRangeEnd)
                                    if (s < e) {
                                        text.setSpan(createSpan(span), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }
                                }
                            }

                            // 3. Apply (for newly typed characters)
                            if (isToggledOn && end > beforeRangeEnd) {
                                text.setSpan(createSpan(null), beforeRangeEnd, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }

                        restoreAndApply(this.typingBold, StyleSpan::class.java, { StyleSpan(it?.style ?: Typeface.BOLD) }, { it.style == Typeface.BOLD })
                        restoreAndApply(this.typingItalic, StyleSpan::class.java, { StyleSpan(it?.style ?: Typeface.ITALIC) }, { it.style == Typeface.ITALIC })
                        restoreAndApply(this.typingUnderline, UnderlineSpan::class.java, { UnderlineSpan() })
                        restoreAndApply(this.typingStrikethrough, StrikethroughSpan::class.java, { StrikethroughSpan() })
                        
                        restoreAndApply(this.typingTextColor != null, ForegroundColorSpan::class.java, { it ?: ForegroundColorSpan(this.typingTextColor ?: 0) })
                        restoreAndApply(this.typingHighlightColor != null, BackgroundColorSpan::class.java, { it ?: BackgroundColorSpan(this.typingHighlightColor ?: 0) })
                    }

                    onContentChanged(SpannableStringBuilder(text ?: ""))
                }

                selectionChangeListener = { start, end ->
                    onSelectionChanged(SpannableStringBuilder(editableText), start, end)
                }
                onEditTextReady(this)
            }
        },
        update = { editText ->
            if (editText is RichEditText) {
                editText.typingBold = typingBold
                editText.typingItalic = typingItalic
                editText.typingUnderline = typingUnderline
                editText.typingStrikethrough = typingStrikethrough
                editText.typingTextColor = typingTextColor
                editText.typingHighlightColor = typingHighlightColor
                
                // Store the latest state to compare against during text changes
                editText.lastContent = content
            }

            if (editText.text.toString() != content.toString()) {
                val selectionStart = editText.selectionStart
                val selectionEnd = editText.selectionEnd
                editText.setText(content)
                val safeStart = selectionStart.coerceIn(0, content.length)
                val safeEnd = selectionEnd.coerceIn(0, content.length)
                editText.setSelection(safeStart, safeEnd)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewRichTextEditor() {
    RichTextEditorTheme {
        RichTextEditor(
            content = SpannableStringBuilder("Hello World"),
            onContentChanged = {},
            onSelectionChanged = { _, _, _ -> },
            onEditTextReady = {},
            typingBold = true,
            typingItalic = false
        )
    }
}
