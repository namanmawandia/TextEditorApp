package com.example.texteditor.ui.editor

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.texteditor.data.DocumentRepository
import com.example.texteditor.data.db.DocumentEntity
import com.example.texteditor.data.SpanSerializer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: DocumentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val documentId: Long = savedStateHandle["documentId"] ?: -1L

    private val _uiState = MutableStateFlow(EditorUiState(documentId = documentId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (documentId != -1L) loadDocument(documentId)
    }

    private fun loadDocument(id: Long) {
        viewModelScope.launch {
            repository.getDocumentById(id)?.let { doc ->
                _uiState.update {
                    it.copy(
                        title = doc.title,
                        content = SpanSerializer.fromHtml(doc.content)
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, isSaved = false) }
    }

    fun onSelectionChanged(spannable: Spannable, start: Int, end: Int) {
        val s = minOf(start, end)
        val e = maxOf(start, end)
        updateToolbarState(spannable, s, e)
    }

    // Called after every span operation to immediately reflect toolbar state
    private fun updateToolbarState(spannable: Spannable, start: Int, end: Int) {
        _uiState.update {
            it.copy(
                isBold = SpanSerializer.hasBold(spannable, start, end),
                isItalic = SpanSerializer.hasItalic(spannable, start, end),
                isUnderline = SpanSerializer.hasUnderline(spannable, start, end),
                isStrikethrough = SpanSerializer.hasStrikethrough(spannable, start, end),
                activeForegroundColor = SpanSerializer.getForegroundColor(spannable, start, end),
                activeHighlightColor = SpanSerializer.getHighlightColor(spannable, start, end)
            )
        }
    }

    fun toggleBold(spannable: SpannableStringBuilder, start: Int, end: Int) {
        toggleStyleSpan(spannable, start, end, Typeface.BOLD)
        updateToolbarState(spannable, start, end)
    }

    fun toggleItalic(spannable: SpannableStringBuilder, start: Int, end: Int) {
        toggleStyleSpan(spannable, start, end, Typeface.ITALIC)
        updateToolbarState(spannable, start, end)
    }

    fun toggleUnderline(spannable: SpannableStringBuilder, start: Int, end: Int) {
        toggleGenericSpan(
            spannable, start, end,
            has = SpanSerializer.hasUnderline(spannable, start, end),
            spanClass = UnderlineSpan::class.java,
            make = { UnderlineSpan() }
        )
        updateToolbarState(spannable, start, end)
    }

    fun toggleStrikethrough(spannable: SpannableStringBuilder, start: Int, end: Int) {
        toggleGenericSpan(
            spannable, start, end,
            has = SpanSerializer.hasStrikethrough(spannable, start, end),
            spanClass = StrikethroughSpan::class.java,
            make = { StrikethroughSpan() }
        )
        updateToolbarState(spannable, start, end)
    }

    fun applyForegroundColor(spannable: SpannableStringBuilder, start: Int, end: Int, color: Int) {
        if (start >= end || start < 0 || end > spannable.length) return
        removeSpansInRange(spannable, start, end, ForegroundColorSpan::class.java)
        spannable.setSpan(
            ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        updateToolbarState(spannable, start, end)
    }

    fun applyHighlightColor(spannable: SpannableStringBuilder, start: Int, end: Int, color: Int) {
        if (start >= end || start < 0 || end > spannable.length) return
        removeSpansInRange(spannable, start, end, BackgroundColorSpan::class.java)
        spannable.setSpan(
            BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        updateToolbarState(spannable, start, end)
    }

    fun removeHighlight(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start >= end || start < 0 || end > spannable.length) return
        removeSpansInRange(spannable, start, end, BackgroundColorSpan::class.java)
        updateToolbarState(spannable, start, end)
    }

    // Handles StyleSpan (Bold/Italic) — needs special care since multiple StyleSpans can coexist
    private fun toggleStyleSpan(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        style: Int
    ) {
        if (start >= end || start < 0 || end > spannable.length) return
        val isActive = if (style == Typeface.BOLD)
            SpanSerializer.hasBold(spannable, start, end)
        else
            SpanSerializer.hasItalic(spannable, start, end)

        if (isActive) {
            // Split existing spans around the selection
            splitAndRemoveStyleSpan(spannable, start, end, style)
        } else {
            spannable.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }

    // Handles simple spans (Underline, Strikethrough)
    private fun <T : Any> toggleGenericSpan(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        has: Boolean,
        spanClass: Class<T>,
        make: () -> T
    ) {
        if (start >= end || start < 0 || end > spannable.length) return
        if (has) {
            splitAndRemoveGenericSpan(spannable, start, end, spanClass, make)
        } else {
            spannable.setSpan(make(), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }

    // Splits a StyleSpan around [start, end) — preserves formatting outside selection
    private fun splitAndRemoveStyleSpan(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        style: Int
    ) {
        val spans = spannable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == style }

        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)

            // Re-apply span on the part BEFORE the selection
            if (spanStart < start) {
                spannable.setSpan(
                    StyleSpan(style), spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
            // Re-apply span on the part AFTER the selection
            if (spanEnd > end) {
                spannable.setSpan(
                    StyleSpan(style), end, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        }
    }

    private fun <T : Any> splitAndRemoveGenericSpan(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        spanClass: Class<T>,
        make: () -> T
    ) {
        val spans = spannable.getSpans(start, end, spanClass)

        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)

            if (spanStart < start) {
                spannable.setSpan(make(), spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            if (spanEnd > end) {
                spannable.setSpan(make(), end, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
    }

    // Removes spans in range while preserving parts outside the range
    private fun <T : Any> removeSpansInRange(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        spanClass: Class<T>
    ) {
        val spans = spannable.getSpans(start, end, spanClass)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if (spanStart < start) {
                spannable.setSpan(
                    spannable.javaClass.getDeclaredConstructor().newInstance(),
                    spanStart, start,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
            if (spanEnd > end) {
                spannable.setSpan(
                    spannable.javaClass.getDeclaredConstructor().newInstance(),
                    end, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        }
    }

    fun saveDocument(liveContent: SpannableStringBuilder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val html = SpanSerializer.toHtml(liveContent)
            if (state.documentId == -1L) {
                val newId = repository.saveDocument(
                    title = state.title.ifBlank { "Untitled" },
                    content = html
                )
                _uiState.update { it.copy(documentId = newId, isSaving = false, isSaved = true) }
            } else {
                repository.updateDocument(
                    DocumentEntity(
                        id = state.documentId,
                        title = state.title.ifBlank { "Untitled" },
                        content = html
                    )
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }
        }
    }
}