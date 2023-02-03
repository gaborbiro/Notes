@file:OptIn(ExperimentalFoundationApi::class)

package dev.gaborbiro.notes.features.notes

import Record
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Transformations
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.ui.theme.NotesTheme
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import dev.gaborbiro.notes.util.BitmapLoader
import kotlinx.coroutines.launch

class NotesActivity : ComponentActivity() {

    private val recordsRepository by lazy { RecordsRepository.get(this) }
    private val bitmapLoader: BitmapLoader by lazy { BitmapLoader(this) }
    private val mapper: RecordsUIMapper by lazy { RecordsUIMapper(bitmapLoader) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val records = recordsRepository.getRecordsLiveData()
        val recordsUIModels = Transformations.map(records, mapper::map)

        setContent {
            NotesTheme {
                Records(
                    records = recordsUIModels
                        .observeAsState(initial = emptyList())
                        .value
                )
            }
        }
    }

    @Composable
    fun Records(records: List<RecordUIModel>) {
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
                Record(
                    context = this@NotesActivity,
                    record = records[it],
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        val showButton = remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                AnimatedVisibility(
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    visible = showButton.value
                ) {
                    Button(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(all = PaddingDefault),
                        onClick = {
                            coroutineScope.launch {
                                listState.scrollToItem(0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text(text = "Scroll to top")
                    }
                }
            }
        }
    }
}