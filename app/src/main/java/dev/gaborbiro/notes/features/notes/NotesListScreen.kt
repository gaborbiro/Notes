@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package dev.gaborbiro.notes.features.notes

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.features.notes.views.RecordView
import dev.gaborbiro.notes.features.widget.NotesWidgetsUpdater
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    context: Context,
    repository: RecordsRepository,
    uiMapper: RecordsUIMapper,
    navigator: NotesListNavigator,
) {
    val coroutineScope = rememberCoroutineScope()
    var searchTerm by remember { mutableStateOf(null as String?) }
    val recordsFlow = repository.getRecordsFlow(searchTerm)
    val records = recordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            SearchFAB {
                searchTerm = it
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
    ) {
        NotesList(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxSize()
                .padding(it),
            records.value.map { uiMapper.map(it) },
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
            onDeleteRecord = { record: RecordUIModel ->
                coroutineScope.launch {
                    val oldRecord = repository.deleteRecord(recordId = record.recordId)

                    NotesWidgetsUpdater.oneOffUpdate(context)
                    val result = snackbarHostState.showSnackbar(
                        message = "Note deleted",
                        actionLabel = "Undo",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            repository.saveRecord(oldRecord)
                        }

                        SnackbarResult.Dismissed -> {
                            val (templateDeleted, imageDeleted) = repository.deleteTemplateIfUnused(
                                oldRecord.template.id
                            )
                            Log.d(
                                "Notes",
                                "template deleted: $templateDeleted, image deleted: $imageDeleted"
                            )
                        }
                    }
                    NotesWidgetsUpdater.oneOffUpdate(context)
                }
            })
    }
}

@Composable
private fun SearchFAB(onSearch: (String?) -> Unit) {
    var fabExpanded by remember { mutableStateOf(false) }
    var text by remember {
        mutableStateOf("")
    }
    val focusRequester = remember { FocusRequester() }

    FloatingActionButton(
        onClick = {
            fabExpanded = fabExpanded.not()
            if (fabExpanded.not()) {
                onSearch(null)
            }
        },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = fabExpanded) {
                TextField(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            focusRequester.requestFocus() // IMPORTANT
                        },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = {
                        Text(
                            text = "Search",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    value = text,
                    singleLine = true,
                    onValueChange = {
                        text = it
                        onSearch(it)
                    },
                )
            }
            Icon(
                imageVector = if (!fabExpanded) Icons.Filled.Search else Icons.Filled.Close,
                contentDescription = "search",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(PaddingDefault)
            )
        }
    }
}

@Composable
private fun NotesList(
    modifier: Modifier,
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
        modifier = modifier
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