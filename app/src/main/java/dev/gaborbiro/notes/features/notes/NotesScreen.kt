@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package dev.gaborbiro.notes.features.notes

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
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
import dev.gaborbiro.notes.features.notes.views.RecordView
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
    val viewModel = remember {
        NotesViewModel(repository, uiMapper, navigator)
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.loadRecords(search = null)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.refreshWidget) {
        viewModel.onWidgetRefreshed()
        NotesWidgetsUpdater.oneOffUpdate(context)
    }

    LaunchedEffect(key1 = uiState.showUndoDeleteSnackbar) {
        if (uiState.showUndoDeleteSnackbar) {
            viewModel.onUndoDeleteSnackbarShown()
            val result = snackbarHostState.showSnackbar(
                message = "Note deleted",
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.onUndoDeleteRequested()
                SnackbarResult.Dismissed -> viewModel.onUndoDeleteDismissed()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            SearchFAB {
                viewModel.loadRecords(search = it)
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
            viewModel,
        )
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
    viewModel: NotesViewModel,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val records = uiState.records

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(PaddingDefault),
        contentPadding = PaddingValues(top = PaddingDefault, bottom = 64.dp),
        state = listState,
        modifier = modifier
    ) {
        items(records.size, key = { records[it].recordId }) {
            RecordView(
                modifier = Modifier.animateItemPlacement(),
                record = records[it],
                onDuplicateRecord = { record ->
                    viewModel.onDuplicateRecordRequested(record)
                    coroutineScope.launch {
                        delay(200L)
                        listState.scrollToItem(0)
                    }
                },
                onUpdateImage = viewModel::onUpdateImageRequested,
                onDeleteImage = viewModel::onDeleteImageRequested,
                onEditRecord = viewModel::onEditRecordRequested,
                onDeleteRecord = viewModel::onDeleteRecordRequested
            )
        }
    }
    ScrollToTopView()
}