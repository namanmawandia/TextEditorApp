package com.example.texteditor.ui.editor

import android.text.SpannableStringBuilder
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
        content = uiState.content,
        isBold = uiState.isBold,
        isItalic = uiState.isItalic,
        isUnderline = uiState.isUnderline,
        isStrikethrough = uiState.isStrikethrough,
        activeForegroundColor = uiState.activeForegroundColor,
        activeHighlightColor = uiState.activeHighlightColor,
        isSaved = uiState.isSaved,
        onTitleChange = viewModel::onTitleChange,
        onContentChanged = { /* content managed via spannable directly */ },
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
        onSave = viewModel::saveDocument,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorContent(
    title: String,
    content: SpannableStringBuilder,
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
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Track current selection for toolbar actions
    var selectionStart by remember { mutableIntStateOf(0) }
    var selectionEnd by remember { mutableIntStateOf(0) }

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
                    IconButton(onClick = onSave) {
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
                onBoldClick = { onBoldClick(content, selectionStart, selectionEnd) },
                onItalicClick = { onItalicClick(content, selectionStart, selectionEnd) },
                onUnderlineClick = { onUnderlineClick(content, selectionStart, selectionEnd) },
                onStrikethroughClick = {
                    onStrikethroughClick(content, selectionStart, selectionEnd)
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
                content = content,
                onContentChanged = onContentChanged,
                onSelectionChanged = { spannable, start, end ->
                    selectionStart = start
                    selectionEnd = end
                    onSelectionChanged(spannable, start, end)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    // Color picker bottom sheet
    colorPickerMode?.let { mode ->
        ColorPickerBottomSheet(
            mode = mode,
            onColorSelected = { color ->
                when (mode) {
                    ColorPickerMode.TextColor ->
                        onTextColorSelected(content, selectionStart, selectionEnd, color)
                    ColorPickerMode.Highlight ->
                        onHighlightColorSelected(content, selectionStart, selectionEnd, color)
                }
            },
            onClear = { onHighlightRemoved(content, selectionStart, selectionEnd) },
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
            content = SpannableStringBuilder(""),
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
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Editor — with content")
@Composable
private fun PreviewEditorWithContent() {
    RichTextEditorTheme {
        EditorContent(
            title = "Meeting Notes",
            content = SpannableStringBuilder(
                "Discussed Q3 roadmap and upcoming feature releases."
            ),
            isBold = true,
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
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Editor — dark theme")
@Composable
private fun PreviewEditorDark() {
    RichTextEditorTheme(darkTheme = true) {
        EditorContent(
            title = "My Notes",
            content = SpannableStringBuilder("Dark mode content preview."),
            isBold = false,
            isItalic = true,
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
            onNavigateBack = {}
        )
    }
}