package com.example.texteditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.texteditor.ui.theme.RichTextEditorTheme

enum class ColorPickerMode { TextColor, Highlight }

private val textColors = listOf(
    Color.Black, Color.DarkGray, Color.Gray,
    Color(0xFFE53935), Color(0xFFD81B60), Color(0xFF8E24AA),
    Color(0xFF1E88E5), Color(0xFF00ACC1), Color(0xFF43A047),
    Color(0xFFFDD835), Color(0xFFFB8C00), Color(0xFF6D4C41)
)

private val highlightColors = listOf(
    Color(0xFFFFEB3B), Color(0xFFFFF176), Color(0xFFA5D6A7),
    Color(0xFF80DEEA), Color(0xFFEF9A9A), Color(0xFFCE93D8),
    Color(0xFF90CAF9), Color(0xFFFFCC80), Color(0xFFF48FB1),
    Color(0xFFB0BEC5), Color(0xFFFFFFFF), Color(0xFFE0E0E0)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerBottomSheet(
    mode: ColorPickerMode,
    onColorSelected: (Int) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val colors = if (mode == ColorPickerMode.TextColor) textColors else highlightColors
    val title = if (mode == ColorPickerMode.TextColor) "Text color" else "Highlight color"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                colors.forEach { color ->
                    ColorSwatch(
                        color = color,
                        onClick = {
                            onColorSelected(color.toArgb())
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mode == ColorPickerMode.Highlight) {
                TextButton(
                    onClick = {
                        onClear()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Remove highlight")
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "Color picker — text color")
@Composable
private fun PreviewColorPickerTextColor() {
    RichTextEditorTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Text color",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                textColors.forEach { color ->
                    ColorSwatch(color = color, onClick = {})
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "Color picker — highlight")
@Composable
private fun PreviewColorPickerHighlight() {
    RichTextEditorTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Highlight color",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                highlightColors.forEach { color ->
                    ColorSwatch(color = color, onClick = {})
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {},
                modifier = Modifier.align(Alignment.End)
            ) { Text("Remove highlight") }
        }
    }
}