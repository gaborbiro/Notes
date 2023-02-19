@file:OptIn(ExperimentalFoundationApi::class)

package dev.gaborbiro.notes.features.notes

import RecordView
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.delay
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
        onDuplicateRecord = { record ->
            coroutineScope.launch {
                repository.duplicateRecord(record.recordId)
                NotesWidgetsUpdater.oneOffUpdate(context)
            }
        },
        onUpdateImage = { record ->
            navigator.updateRecordPhoto(record.templateId)
        },
        onDeleteImage = { record ->
            coroutineScope.launch {
                repository.deleteImage(record.templateId)
                NotesWidgetsUpdater.oneOffUpdate(context)
            }
        },
        onEditRecord = { record ->
            navigator.editRecord(recordId = record.recordId)
        },
        onDeleteRecord = { record ->
            coroutineScope.launch {
                val (templateDeleted, imageDeleted) = repository.deleteRecordAndCleanupTemplate(
                    record.recordId
                )
                Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
                NotesWidgetsUpdater.oneOffUpdate(context)
            }
        })
}

@Composable
private fun NotesList(
    records: List<RecordUIModel>,
    onDuplicateRecord: (RecordUIModel) -> Unit,
    onUpdateImage: (RecordUIModel) -> Unit,
    onDeleteImage: (RecordUIModel) -> Unit,
    onEditRecord: (RecordUIModel) -> Unit,
    onDeleteRecord: (RecordUIModel) -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
                onDuplicateRecord = {
                    onDuplicateRecord(record)
                    coroutineScope.launch {
                        delay(200L)
                        listState.scrollToItem(0)
                    }
                },
                onUpdateImage = { onUpdateImage(record) },
                onDeleteImage = { onDeleteImage(record) },
                onEditRecord = { onEditRecord(record) },
                onDeleteRecord = { onDeleteRecord(record) }
            )
        }
    }
    ScrollToTopView()
}