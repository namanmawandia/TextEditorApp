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
                        content = SpannableStringBuilder(
                            SpanSerializer.fromHtml(doc.content)
                        )
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, isSaved = false) }
    }

    // Called whenever selection changes in the editor — updates toolbar toggle states
    fun onSelectionChanged(spannable: Spannable, start: Int, end: Int) {
        val s = minOf(start, end)
        val e = maxOf(start, end)
        _uiState.update {
            it.copy(
                isBold = SpanSerializer.hasBold(spannable, s, e),
                isItalic = SpanSerializer.hasItalic(spannable, s, e),
                isUnderline = SpanSerializer.hasUnderline(spannable, s, e),
                isStrikethrough = SpanSerializer.hasStrikethrough(spannable, s, e),
                activeForegroundColor = SpanSerializer.getForegroundColor(spannable, s, e),
                activeHighlightColor = SpanSerializer.getHighlightColor(spannable, s, e)
            )
        }
    }

    fun toggleBold(spannable: SpannableStringBuilder, start: Int, end: Int) =
        toggleSpan(spannable, start, end,
            has = { SpanSerializer.hasBold(it, start, end) },
            add = { StyleSpan(Typeface.BOLD) }
        )

    fun toggleItalic(spannable: SpannableStringBuilder, start: Int, end: Int) =
        toggleSpan(spannable, start, end,
            has = { SpanSerializer.hasItalic(it, start, end) },
            add = { StyleSpan(Typeface.ITALIC) }
        )

    fun toggleUnderline(spannable: SpannableStringBuilder, start: Int, end: Int) =
        toggleSpan(spannable, start, end,
            has = { SpanSerializer.hasUnderline(it, start, end) },
            add = { UnderlineSpan() }
        )

    fun toggleStrikethrough(spannable: SpannableStringBuilder, start: Int, end: Int) =
        toggleSpan(spannable, start, end,
            has = { SpanSerializer.hasStrikethrough(it, start, end) },
            add = { StrikethroughSpan() }
        )

    fun applyForegroundColor(spannable: SpannableStringBuilder, start: Int, end: Int, color: Int) {
        if (start >= end) return
        spannable.getSpans(start, end, ForegroundColorSpan::class.java)
            .forEach { spannable.removeSpan(it) }
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        notifyContentChanged(spannable)
    }

    fun applyHighlightColor(spannable: SpannableStringBuilder, start: Int, end: Int, color: Int) {
        if (start >= end) return
        spannable.getSpans(start, end, BackgroundColorSpan::class.java)
            .forEach { spannable.removeSpan(it) }
        spannable.setSpan(BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        notifyContentChanged(spannable)
    }

    fun removeHighlight(spannable: SpannableStringBuilder, start: Int, end: Int) {
        if (start >= end) return
        spannable.getSpans(start, end, BackgroundColorSpan::class.java)
            .forEach { spannable.removeSpan(it) }
        notifyContentChanged(spannable)
    }

    private fun toggleSpan(
        spannable: SpannableStringBuilder,
        start: Int,
        end: Int,
        has: (SpannableStringBuilder) -> Boolean,
        add: () -> Any
    ) {
        if (start >= end) return
        if (has(spannable)) {
            val spanClass = add()::class.java
            spannable.getSpans(start, end, spanClass).forEach { spannable.removeSpan(it) }
        } else {
            spannable.setSpan(add(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        notifyContentChanged(spannable)
    }

    private fun notifyContentChanged(spannable: SpannableStringBuilder) {
        _uiState.update { it.copy(content = spannable, isSaved = false) }
    }

    fun saveDocument() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value
            val html = SpanSerializer.toHtml(state.content)
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