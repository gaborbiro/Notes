package dev.gaborbiro.notes.features.notes

import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.features.common.model.RecordUIModel

data class NotesUIState(
    val records: List<RecordUIModel> = emptyList(),
    val refreshWidget: Boolean = false,
    val showUndoDeleteSnackbar: Boolean = false,
    val recordToUndelete: Record? = null,
)