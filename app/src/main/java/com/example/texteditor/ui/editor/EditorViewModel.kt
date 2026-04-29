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

    fun onContentChanged(spannable: Spannable) {
        _uiState.update { state ->
            // Update content in state to prevent "erasing" during recomposition
            if (state.content.toString() != spannable.toString()) {
                state.copy(
                    content = SpannableStringBuilder(spannable),
                    isSaved = false
                )
            } else state
        }
    }

    fun onSelectionChanged(spannable: Spannable, start: Int, end: Int) {
        val s = minOf(start, end)
        val e = maxOf(start, end)

        _uiState.update {
            it.copy(
                content = SpannableStringBuilder(spannable),
                selectionStart = s,
                selectionEnd = e
            )
        }

        updateToolbarState(spannable, s, e)
    }

    private fun updateToolbarState(spannable: Spannable, start: Int, end: Int) {
        _uiState.update { state ->
            val isCursor = start == end
            
            state.copy(
                isBold = if (isCursor) state.typingBold else SpanSerializer.hasBold(spannable, start, end),
                isItalic = if (isCursor) state.typingItalic else SpanSerializer.hasItalic(spannable, start, end),
                isUnderline = if (isCursor) state.typingUnderline else SpanSerializer.hasUnderline(spannable, start, end),
                isStrikethrough = if (isCursor) state.typingStrikethrough else SpanSerializer.hasStrikethrough(spannable, start, end),
                activeForegroundColor = if (isCursor) state.typingTextColor else SpanSerializer.getForegroundColor(spannable, start, end),
                activeHighlightColor = if (isCursor) state.typingHighlightColor else SpanSerializer.getHighlightColor(spannable, start, end)
            )
        }
    }

    fun toggleBold(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingBold = !it.typingBold, content = SpannableStringBuilder(spannable)) }
        } else {
            toggleStyleSpan(spannable, start, end, Typeface.BOLD)
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    fun toggleItalic(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingItalic = !it.typingItalic, content = SpannableStringBuilder(spannable)) }
        } else {
            toggleStyleSpan(spannable, start, end, Typeface.ITALIC)
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    fun toggleUnderline(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingUnderline = !it.typingUnderline, content = SpannableStringBuilder(spannable)) }
        } else {
            toggleGenericSpan(
                spannable, start, end,
                has = SpanSerializer.hasUnderline(spannable, start, end),
                spanClass = UnderlineSpan::class.java,
                make = { UnderlineSpan() }
            )
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    fun toggleStrikethrough(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingStrikethrough = !it.typingStrikethrough, content = SpannableStringBuilder(spannable)) }
        } else {
            toggleGenericSpan(
                spannable, start, end,
                has = SpanSerializer.hasStrikethrough(spannable, start, end),
                spanClass = StrikethroughSpan::class.java,
                make = { StrikethroughSpan() }
            )
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    fun applyForegroundColor(spannable: SpannableStringBuilder, start: Int, end: Int, color: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingTextColor = color, content = SpannableStringBuilder(spannable)) }
        } else {
            splitColorSpan(
                spannable, start, end,
                spanClass = ForegroundColorSpan::class.java,
                makeWithColor = { ForegroundColorSpan(it) },
                getColor = { it.foregroundColor }
            )
            spannable.setSpan(
                ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    fun applyHighlightColor(spannable: SpannableStringBuilder, start: Int, end: Int, color: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingHighlightColor = color, content = SpannableStringBuilder(spannable)) }
        } else {
            splitColorSpan(
                spannable, start, end,
                spanClass = BackgroundColorSpan::class.java,
                makeWithColor = { BackgroundColorSpan(it) },
                getColor = { it.backgroundColor }
            )
            spannable.setSpan(
                BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    fun removeHighlight(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start == end) {
            _uiState.update { it.copy(typingHighlightColor = null, content = SpannableStringBuilder(spannable)) }
        } else {
            splitColorSpan(
                spannable, start, end,
                spanClass = BackgroundColorSpan::class.java,
                makeWithColor = { BackgroundColorSpan(it) },
                getColor = { it.backgroundColor }
            )
            syncContent(spannable)
        }
        updateToolbarState(spannable, start, end)
    }

    private fun syncContent(spannable: SpannableStringBuilder) {
        _uiState.update { it.copy(content = SpannableStringBuilder(spannable), isSaved = false) }
    }

    private fun <T : Any> splitColorSpan(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        spanClass: Class<T>,
        makeWithColor: (Int) -> T,
        getColor: (T) -> Int
    ) {
        val spans = spannable.getSpans(start, end, spanClass)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span)
            val spanEnd = spannable.getSpanEnd(span)
            val color = getColor(span)
            spannable.removeSpan(span)

            if (spanStart < start) {
                spannable.setSpan(makeWithColor(color), spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            if (spanEnd > end) {
                spannable.setSpan(makeWithColor(color), end, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
    }

    private fun toggleStyleSpan(spannable: SpannableStringBuilder, start: Int, end: Int, style: Int) {
        if (start >= end || start < 0 || end > spannable.length) return
        val isActive = if (style == Typeface.BOLD) SpanSerializer.hasBold(spannable, start, end)
        else SpanSerializer.hasItalic(spannable, start, end)

        if (isActive) splitAndRemoveStyleSpan(spannable, start, end, style)
        else spannable.setSpan(StyleSpan(style), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    private fun <T : Any> toggleGenericSpan(spannable: SpannableStringBuilder, start: Int, end: Int, has: Boolean, spanClass: Class<T>, make: () -> T) {
        if (start >= end || start < 0 || end > spannable.length) return
        if (has) splitAndRemoveGenericSpan(spannable, start, end, spanClass, make)
        else spannable.setSpan(make(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    private fun splitAndRemoveStyleSpan(spannable: SpannableStringBuilder, start: Int, end: Int, style: Int) {
        val spans = spannable.getSpans(start, end, StyleSpan::class.java).filter { it.style == style }
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span); val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if (spanStart < start) spannable.setSpan(StyleSpan(style), spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            if (spanEnd > end) spannable.setSpan(StyleSpan(style), end, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }

    private fun <T : Any> splitAndRemoveGenericSpan(spannable: SpannableStringBuilder, start: Int, end: Int, spanClass: Class<T>, make: () -> T) {
        val spans = spannable.getSpans(start, end, spanClass)
        for (span in spans) {
            val spanStart = spannable.getSpanStart(span); val spanEnd = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            if (spanStart < start) spannable.setSpan(make(), spanStart, start, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            if (spanEnd > end) spannable.setSpan(make(), end, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }

    fun saveDocument(liveContent: SpannableStringBuilder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val html = SpanSerializer.toHtml(liveContent)
            if (state.documentId == -1L) {
                val newId = repository.saveDocument(title = state.title.ifBlank { "Untitled" }, content = html)
                _uiState.update { it.copy(documentId = newId, isSaving = false, isSaved = true, content = SpannableStringBuilder(liveContent)) }
            } else {
                repository.updateDocument(DocumentEntity(id = state.documentId, title = state.title.ifBlank { "Untitled" }, content = html))
                _uiState.update { it.copy(isSaving = false, isSaved = true, content = SpannableStringBuilder(liveContent)) }
            }
        }
    }
}
