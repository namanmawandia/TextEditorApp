package com.example.texteditor.ui.documentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.texteditor.data.DocumentRepository
import com.example.texteditor.data.db.DocumentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentListViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    val documents: StateFlow<List<DocumentEntity>> =
        repository.getAllDocuments()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch { repository.deleteDocument(doc) }
    }
}