package com.example.texteditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.texteditor.ui.theme.RichTextEditorTheme

@Composable
fun FormattingToolbar(
    isBold: Boolean,
    isItalic: Boolean,
    isUnderline: Boolean,
    isStrikethrough: Boolean,
    activeForegroundColor: Int?,
    activeHighlightColor: Int?,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onHighlightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FormatButton(
                icon = Icons.Default.FormatBold,
                isActive = isBold,
                contentDescription = "Bold",
                onClick = onBoldClick
            )
            FormatButton(
                icon = Icons.Default.FormatItalic,
                isActive = isItalic,
                contentDescription = "Italic",
                onClick = onItalicClick
            )
            FormatButton(
                icon = Icons.Default.FormatUnderlined,
                isActive = isUnderline,
                contentDescription = "Underline",
                onClick = onUnderlineClick
            )
            FormatButton(
                icon = Icons.Default.FormatStrikethrough,
                isActive = isStrikethrough,
                contentDescription = "Strikethrough",
                onClick = onStrikethroughClick
            )

            Spacer(modifier = Modifier.width(4.dp))
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(4.dp))

           ColorFormatButton(
                icon = Icons.Default.FormatColorText,
                activeColor = activeForegroundColor?.let { Color(it) },
                contentDescription = "Text color",
                onClick = onTextColorClick
            )
            ColorFormatButton(
                icon = Icons.Default.FormatColorText,
                activeColor = activeHighlightColor?.let { Color(it) },
                contentDescription = "Highlight",
                indicatorBelow = true,
                onClick = onHighlightClick
            )
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    isActive: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                Color.Transparent,
            contentColor = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ColorFormatButton(
    icon: ImageVector,
    activeColor: Color?,
    contentDescription: String,
    indicatorBelow: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = activeColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
            // Small color dot indicator below the icon
            if (activeColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Toolbar — all inactive")
@Composable
private fun PreviewToolbarInactive() {
    RichTextEditorTheme {
        FormattingToolbar(
            isBold = false,
            isItalic = false,
            isUnderline = false,
            isStrikethrough = false,
            activeForegroundColor = null,
            activeHighlightColor = null,
            onBoldClick = {},
            onItalicClick = {},
            onUnderlineClick = {},
            onStrikethroughClick = {},
            onTextColorClick = {},
            onHighlightClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Toolbar — bold and italic active")
@Composable
private fun PreviewToolbarBoldItalic() {
    RichTextEditorTheme {
        FormattingToolbar(
            isBold = true,
            isItalic = true,
            isUnderline = false,
            isStrikethrough = false,
            activeForegroundColor = null,
            activeHighlightColor = null,
            onBoldClick = {},
            onItalicClick = {},
            onUnderlineClick = {},
            onStrikethroughClick = {},
            onTextColorClick = {},
            onHighlightClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Toolbar — all active with colors")
@Composable
private fun PreviewToolbarAllActive() {
    RichTextEditorTheme {
        FormattingToolbar(
            isBold = true,
            isItalic = true,
            isUnderline = true,
            isStrikethrough = true,
            activeForegroundColor = 0xFFE53935.toInt(),
            activeHighlightColor = 0xFFFFEB3B.toInt(),
            onBoldClick = {},
            onItalicClick = {},
            onUnderlineClick = {},
            onStrikethroughClick = {},
            onTextColorClick = {},
            onHighlightClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Toolbar — dark theme")
@Composable
private fun PreviewToolbarDark() {
    RichTextEditorTheme(darkTheme = true) {
        FormattingToolbar(
            isBold = true,
            isItalic = false,
            isUnderline = false,
            isStrikethrough = false,
            activeForegroundColor = null,
            activeHighlightColor = null,
            onBoldClick = {},
            onItalicClick = {},
            onUnderlineClick = {},
            onStrikethroughClick = {},
            onTextColorClick = {},
            onHighlightClick = {}
        )
    }
}