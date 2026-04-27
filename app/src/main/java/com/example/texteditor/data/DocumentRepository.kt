package com.example.texteditor.data

import com.example.texteditor.data.db.DocumentDao
import com.example.texteditor.data.db.DocumentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val dao: DocumentDao
) {
    fun getAllDocuments(): Flow<List<DocumentEntity>> = dao.getAllDocuments()

    suspend fun getDocumentById(id: Long): DocumentEntity? =
        withContext(Dispatchers.IO) { dao.getDocumentById(id) }

    suspend fun saveDocument(title: String, content: String): Long =
        withContext(Dispatchers.IO) {
            dao.insertDocument(DocumentEntity(title = title, content = content))
        }

    suspend fun updateDocument(doc: DocumentEntity) =
        withContext(Dispatchers.IO) {
            dao.updateDocument(doc.copy(updatedAt = System.currentTimeMillis()))
        }

    suspend fun deleteDocument(doc: DocumentEntity) =
        withContext(Dispatchers.IO) { dao.deleteDocument(doc) }
}