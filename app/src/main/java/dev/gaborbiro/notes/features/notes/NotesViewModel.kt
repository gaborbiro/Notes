package dev.gaborbiro.notes.features.notes

import android.util.Log
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: RecordsRepository,
    private val uiMapper: RecordsUIMapper,
    private val navigator: NotesListNavigator,
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<NotesUIState> = MutableStateFlow(NotesUIState())
    val uiState: StateFlow<NotesUIState> = _uiState.asStateFlow()

    fun loadRecords(search: String?) {
        viewModelScope.launch {
            repository.getRecordsFlow(search)
                .map {
                    uiMapper.map(it, thumbnail = true)
                }
                .collect { records ->
                    _uiState.update {
                        it.copy(records)
                    }
                }
        }
    }

    fun onDuplicateRecordRequested(record: RecordUIModel) {
        viewModelScope.launch {
            repository.duplicateRecord(record.recordId)
        }
        refreshWidget()
    }

    fun onUpdateImageRequested(record: RecordUIModel) {
        navigator.updateRecordPhoto(record.recordId)
    }

    fun onDeleteImageRequested(record: RecordUIModel) {
        viewModelScope.launch {
            repository.deleteImage(record.templateId)
        }
        refreshWidget()
    }

    fun onEditRecordRequested(record: RecordUIModel) {
        navigator.editRecord(recordId = record.recordId)
    }

    fun onDeleteRecordRequested(record: RecordUIModel) {
        viewModelScope.launch {
            val oldRecord = repository.deleteRecord(recordId = record.recordId)
            _uiState.update {
                it.copy(
                    showUndoDeleteSnackbar = true,
                    recordToUndelete = oldRecord,
                )
            }
            refreshWidget()
        }
    }

    fun onImageTapped(record: RecordUIModel) {
        navigator.viewImage(record.recordId)
    }

    fun onUndoDeleteRequested() {
        viewModelScope.launch {
            repository.saveRecord(_uiState.value.recordToUndelete!!)
        }
        _uiState.update {
            it.copy(
                recordToUndelete = null,
            )
        }
    }

    fun onUndoDeleteDismissed() {
        viewModelScope.launch {
            val (templateDeleted, imageDeleted) = repository.deleteTemplateIfUnused(
                _uiState.value.recordToUndelete!!.template.id
            )
            Log.d(
                "Notes",
                "template deleted: $templateDeleted, image deleted: $imageDeleted"
            )
        }
        _uiState.update {
            it.copy(
                recordToUndelete = null,
            )
        }
    }

    private fun refreshWidget() {
        _uiState.update {
            it.copy(refreshWidget = true)
        }
    }

    fun onWidgetRefreshed() {
        _uiState.update {
            it.copy(refreshWidget = false)
        }
    }

    fun onUndoDeleteSnackbarShown() {
        _uiState.update {
            it.copy(
                showUndoDeleteSnackbar = false
            )
        }
    }
}
