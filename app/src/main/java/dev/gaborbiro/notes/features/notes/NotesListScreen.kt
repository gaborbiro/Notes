@file:OptIn(ExperimentalFoundationApi::class)

package dev.gaborbiro.notes.features.notes

import RecordView
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.features.widget.NotesWidgetsUpdater
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import kotlinx.coroutines.launch

@Composable
fun NotesListScreen(
    context: Context,
    repository: RecordsRepository,
    uiMapper: RecordsUIMapper,
    navigator: NotesListNavigator,
) {
    val coroutineScope = rememberCoroutineScope()
    val records = repository.getRecordsFlow()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    NotesList(records.value.map { uiMapper.map(it) },
        onUpdateImage = { record ->
            navigator.updateRecordPhoto(record.templateId)
        },
        onDeleteImage = { record ->
            coroutineScope.launch {
                repository.updateTemplatePhoto(record.templateId, uri = null)
                NotesWidgetsUpdater.oneOffUpdate(context)
            }
        },
        onDeleteRecord = { record ->
            coroutineScope.launch {
                repository.deleteRecord(record.recordId)
                NotesWidgetsUpdater.oneOffUpdate(context)
            }
        })
}

@Composable
private fun NotesList(
    records: List<RecordUIModel>,
    onUpdateImage: (RecordUIModel) -> Unit,
    onDeleteImage: (RecordUIModel) -> Unit,
    onDeleteRecord: (RecordUIModel) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(PaddingDefault),
        contentPadding = PaddingValues(top = PaddingDefault, bottom = 64.dp),
        state = listState,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        items(records.size, key = { records[it].recordId }) {
            val record = records[it]
            RecordView(
                modifier = Modifier.animateItemPlacement(),
                record = record,
                onUpdateImage = { onUpdateImage(record) },
                onDeleteImage = { onDeleteImage(record) },
                onDeleteRecord = { onDeleteRecord(record) }
            )
        }
    }
    ScrollToTopView(listState, rememberCoroutineScope())
}