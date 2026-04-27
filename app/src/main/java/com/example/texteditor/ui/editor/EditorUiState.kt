package com.example.texteditor.ui.editor

import android.text.SpannableStringBuilder

data class EditorUiState(
    val documentId: Long = -1L,
    val title: String = "",
    val content: SpannableStringBuilder = SpannableStringBuilder(),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val activeForegroundColor: Int? = null,
    val activeHighlightColor: Int? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)