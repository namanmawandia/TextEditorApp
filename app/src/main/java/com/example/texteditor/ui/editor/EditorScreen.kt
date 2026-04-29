package com.example.texteditor.ui.editor

import android.text.SpannableStringBuilder
import android.widget.EditText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.texteditor.ui.theme.RichTextEditorTheme

@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditorContent(
        title = uiState.title,
        initialContent = uiState.content,
        isBold = uiState.isBold,
        isItalic = uiState.isItalic,
        isUnderline = uiState.isUnderline,
        isStrikethrough = uiState.isStrikethrough,
        activeForegroundColor = uiState.activeForegroundColor,
        activeHighlightColor = uiState.activeHighlightColor,
        isSaved = uiState.isSaved,
        onTitleChange = viewModel::onTitleChange,
        onContentChanged = viewModel::onContentChanged,
        onSelectionChanged = { spannable, start, end ->
            viewModel.onSelectionChanged(spannable, start, end)
        },
        onBoldClick = { content, start, end ->
            viewModel.toggleBold(content, start, end)
        },
        onItalicClick = { content, start, end ->
            viewModel.toggleItalic(content, start, end)
        },
        onUnderlineClick = { content, start, end ->
            viewModel.toggleUnderline(content, start, end)
        },
        onStrikethroughClick = { content, start, end ->
            viewModel.toggleStrikethrough(content, start, end)
        },
        onTextColorSelected = { content, start, end, color ->
            viewModel.applyForegroundColor(content, start, end, color)
        },
        onHighlightColorSelected = { content, start, end, color ->
            viewModel.applyHighlightColor(content, start, end, color)
        },
        onHighlightRemoved = { content, start, end ->
            viewModel.removeHighlight(content, start, end)
        },
        onSave = { liveContent ->
            viewModel.saveDocument(liveContent)
        },
        onNavigateBack = onNavigateBack,
        typingBold = uiState.typingBold,
        typingItalic = uiState.typingItalic,
        typingUnderline = uiState.typingUnderline,
        typingStrikethrough = uiState.typingStrikethrough,
        typingTextColor = uiState.typingTextColor,
        typingHighlightColor = uiState.typingHighlightColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorContent(
    title: String,
    initialContent: SpannableStringBuilder,
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean,
    isStrikethrough: Boolean,
    activeForegroundColor: Int?,
    activeHighlightColor: Int?,
    isSaved: Boolean,
    onTitleChange: (String) -> Unit,
    onContentChanged: (SpannableStringBuilder) -> Unit,
    onSelectionChanged: (SpannableStringBuilder, Int, Int) -> Unit,
    onBoldClick: (SpannableStringBuilder, Int, Int) -> Unit,
    onItalicClick: (SpannableStringBuilder, Int, Int) -> Unit,
    onUnderlineClick: (SpannableStringBuilder, Int, Int) -> Unit,
    onStrikethroughClick: (SpannableStringBuilder, Int, Int) -> Unit,
    onTextColorSelected: (SpannableStringBuilder, Int, Int, Int) -> Unit,
    onHighlightColorSelected: (SpannableStringBuilder, Int, Int, Int) -> Unit,
    onHighlightRemoved: (SpannableStringBuilder, Int, Int) -> Unit,
    onSave: (SpannableStringBuilder) -> Unit,
    onNavigateBack: () -> Unit,
    typingBold: Boolean,
    typingItalic: Boolean,
    typingUnderline: Boolean,
    typingStrikethrough: Boolean,
    typingTextColor: Int?,
    typingHighlightColor: Int?
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var selectionStart by remember { mutableIntStateOf(0) }
    var selectionEnd by remember { mutableIntStateOf(0) }
    val liveContent = remember { mutableStateOf(SpannableStringBuilder(initialContent)) }
    val editTextRef = remember { mutableStateOf<EditText?>(null) }

    var colorPickerMode by remember { mutableStateOf<ColorPickerMode?>(null) }

    LaunchedEffect(isSaved) {
        if (isSaved) snackbarHostState.showSnackbar("Saved")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                "Untitled",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val current = editTextRef.value?.editableText
                            ?.let { SpannableStringBuilder(it) }
                            ?: liveContent.value
                        onSave(current)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            FormattingToolbar(
                isBold = isBold,
                isItalic = isItalic,
                isUnderline = isUnderline,
                isStrikethrough = isStrikethrough,
                activeForegroundColor = activeForegroundColor,
                activeHighlightColor = activeHighlightColor,
                onBoldClick = {
                    editTextRef.value?.editableText?.let { editable ->
                        onBoldClick(editable as SpannableStringBuilder, selectionStart, selectionEnd)
                        editTextRef.value?.setSelection(selectionStart, selectionEnd)
                    }
                },
                onItalicClick = {
                    editTextRef.value?.editableText?.let { editable ->
                        onItalicClick(editable as SpannableStringBuilder, selectionStart, selectionEnd)
                        editTextRef.value?.setSelection(selectionStart, selectionEnd)
                    }
                },
                onUnderlineClick = {
                    editTextRef.value?.editableText?.let { editable ->
                        onUnderlineClick(editable as SpannableStringBuilder, selectionStart, selectionEnd)
                        editTextRef.value?.setSelection(selectionStart, selectionEnd)
                    }
                },
                onStrikethroughClick = {
                    editTextRef.value?.editableText?.let { editable ->
                        onStrikethroughClick(editable as SpannableStringBuilder, selectionStart, selectionEnd)
                        editTextRef.value?.setSelection(selectionStart, selectionEnd)
                    }
                },
                onTextColorClick = { colorPickerMode = ColorPickerMode.TextColor },
                onHighlightClick = { colorPickerMode = ColorPickerMode.Highlight },
                modifier = Modifier.imePadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RichTextEditor(
                content = initialContent,
                onContentChanged = { updated ->
                    liveContent.value = updated
                    onContentChanged(updated)
                },
                onSelectionChanged = { spannable, start, end ->
                    selectionStart = start
                    selectionEnd = end
                    liveContent.value = spannable
                    onSelectionChanged(spannable, start, end)
                },
                onEditTextReady = { editText ->
                    editTextRef.value = editText
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
                typingBold = typingBold,
                typingItalic = typingItalic,
                typingUnderline = typingUnderline,
                typingStrikethrough = typingStrikethrough,
                typingTextColor = typingTextColor,
                typingHighlightColor = typingHighlightColor
            )
        }
    }

    colorPickerMode?.let { mode ->
        ColorPickerBottomSheet(
            mode = mode,
            onColorSelected = { color ->
                editTextRef.value?.let { editText ->
                    val editable = editText.editableText as SpannableStringBuilder
                    when (mode) {
                        ColorPickerMode.TextColor ->
                            onTextColorSelected(editable, selectionStart, selectionEnd, color)
                        ColorPickerMode.Highlight ->
                            onHighlightColorSelected(editable, selectionStart, selectionEnd, color)
                    }
                    editText.setSelection(selectionStart, selectionEnd)
                }
            },
            onClear = {
                editTextRef.value?.let { editText ->
                    val editable = editText.editableText as SpannableStringBuilder
                    onHighlightRemoved(editable, selectionStart, selectionEnd)
                    editText.setSelection(selectionStart, selectionEnd)
                }
            },
            onDismiss = { colorPickerMode = null }
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Editor — empty document")
@Composable
private fun PreviewEditorEmpty() {
    RichTextEditorTheme {
        EditorContent(
            title = "",
            initialContent = SpannableStringBuilder(""),
            isBold = false,
            isItalic = false,
            isUnderline = false,
            isStrikethrough = false,
            activeForegroundColor = null,
            activeHighlightColor = null,
            isSaved = false,
            onTitleChange = {},
            onContentChanged = {},
            onSelectionChanged = { _, _, _ -> },
            onBoldClick = { _, _, _ -> },
            onItalicClick = { _, _, _ -> },
            onUnderlineClick = { _, _, _ -> },
            onStrikethroughClick = { _, _, _ -> },
            onTextColorSelected = { _, _, _, _ -> },
            onHighlightColorSelected = { _, _, _, _ -> },
            onHighlightRemoved = { _, _, _ -> },
            onSave = {},
            onNavigateBack = {},
            typingBold = false,
            typingItalic = false,
            typingUnderline = false,
            typingStrikethrough = false,
            typingTextColor = null,
            typingHighlightColor = null
        )
    }
}
