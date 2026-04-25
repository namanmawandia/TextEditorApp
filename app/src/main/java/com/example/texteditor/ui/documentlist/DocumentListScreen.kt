package com.yourapp.editor.ui.documentlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.texteditor.Data.db.DocumentEntity
import com.example.texteditor.ui.documentlist.DocumentListViewModel
import com.example.texteditor.ui.theme.RichTextEditorTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentListScreen(
    onDocumentClick: (Long) -> Unit,
    onNewDocument: () -> Unit,
    viewModel: DocumentListViewModel = hiltViewModel()
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }

    DocumentListContent(
        documents = documents,
        onDocumentClick = onDocumentClick,
        onNewDocument = onNewDocument,
        onDocumentLongClick = { docToDelete = it },
        onDeleteConfirm = {
            docToDelete?.let { viewModel.deleteDocument(it) }
            docToDelete = null
        },
        onDeleteDismiss = { docToDelete = null },
        docToDelete = docToDelete
    )
}

// Separated so Preview can call this without needing hiltViewModel()
@Composable
fun DocumentListContent(
    documents: List<DocumentEntity>,
    onDocumentClick: (Long) -> Unit,
    onNewDocument: () -> Unit,
    onDocumentLongClick: (DocumentEntity) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    docToDelete: DocumentEntity?
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewDocument,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New document",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (documents.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                DocumentList(
                    documents = documents,
                    onDocumentClick = { onDocumentClick(it.id) },
                    onDocumentLongClick = onDocumentLongClick
                )
            }
        }
    }

    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("Delete document?") },
            text = {
                Text("\"${doc.title.ifBlank { "Untitled" }}\" will be permanently deleted.")
            },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DocumentList(
    documents: List<DocumentEntity>,
    onDocumentClick: (DocumentEntity) -> Unit,
    onDocumentLongClick: (DocumentEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "My Documents",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        items(
            items = documents,
            key = { doc -> doc.id }
        ) { doc ->
            DocumentCard(
                document = doc,
                onClick = { onDocumentClick(doc) },
                onLongClick = { onDocumentLongClick(doc) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = document.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (document.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = formatDate(document.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No documents yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap + to create one",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(millis))




// ── Previews ─────────────────────────────────────────────────────────────────

private val sampleDocs = listOf(
    DocumentEntity(
        id = 1L,
        title = "Meeting Notes",
        content = "Discussed Q3 roadmap and upcoming feature releases for the mobile team.",
        updatedAt = System.currentTimeMillis()
    ),
    DocumentEntity(
        id = 2L,
        title = "Shopping List",
        content = "Milk, eggs, bread, coffee, and some fresh vegetables from the market.",
        updatedAt = System.currentTimeMillis() - 86_400_000
    ),
    DocumentEntity(
        id = 3L,
        title = "",   // tests "Untitled" fallback
        content = "",
        updatedAt = System.currentTimeMillis() - 172_800_000
    )
)

@Preview(showBackground = true, name = "List — populated")
@Composable
private fun PreviewDocumentListPopulated() {
    RichTextEditorTheme {
        DocumentListContent(
            documents = sampleDocs,
            onDocumentClick = {},
            onNewDocument = {},
            onDocumentLongClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            docToDelete = null
        )
    }
}

@Preview(showBackground = true, name = "List — empty")
@Composable
private fun PreviewDocumentListEmpty() {
    RichTextEditorTheme {
        DocumentListContent(
            documents = emptyList(),
            onDocumentClick = {},
            onNewDocument = {},
            onDocumentLongClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            docToDelete = null
        )
    }
}

@Preview(showBackground = true, name = "List — delete dialog")
@Composable
private fun PreviewDocumentListDeleteDialog() {
    RichTextEditorTheme {
        DocumentListContent(
            documents = sampleDocs,
            onDocumentClick = {},
            onNewDocument = {},
            onDocumentLongClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            docToDelete = sampleDocs.first()
        )
    }
}

@Preview(showBackground = true, name = "Card — with content")
@Composable
private fun PreviewDocumentCard() {
    RichTextEditorTheme {
        DocumentCard(
            document = sampleDocs[0],
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun PreviewEmptyState() {
    RichTextEditorTheme {
        EmptyState()
    }
}