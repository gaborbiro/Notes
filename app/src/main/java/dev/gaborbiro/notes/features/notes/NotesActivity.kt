@file:OptIn(ExperimentalFoundationApi::class)

package dev.gaborbiro.notes.features.notes

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.coroutineScope
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.features.widget.NotesWidgetsUpdater
import dev.gaborbiro.notes.ui.theme.NotesTheme
import dev.gaborbiro.notes.ui.theme.PaddingHalf
import dev.gaborbiro.notes.util.BitmapLoader
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val recordsRepository by lazy { RecordsRepository.get() }
    private val mapper: RecordsUIMapper by lazy { RecordsUIMapper(BitmapLoader(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val records = remember { mutableStateOf(listOf<RecordUIModel>()) }

            LaunchedEffect(key1 = Unit) {
                records.value = mapper.map(recordsRepository.getRecords())
            }

            NotesTheme {
                Records(records = records.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycle.coroutineScope.launch {
            NotesWidgetsUpdater.oneOffUpdate(this@MainActivity)
        }
    }
}

@Composable
fun Records(records: List<RecordUIModel>) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        state = listState
    ) {
        stickyHeader {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(all = PaddingHalf),
                text = "df"
            )
        }
        items(records.size, key = { records[it].id }) {
            Record(record = records[it], Modifier.animateItemPlacement())
        }
    }
    val showButton = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    AnimatedVisibility(visible = showButton.value) {
        Button(onClick = {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }) {
            Text(text = "Simple Button")
        }
    }
}

@Composable
fun Record(record: RecordUIModel, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            record.bitmap?.let { bitmap: Bitmap ->
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "placeholder",
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
            ) {
                Text(
                    text = record.timestamp,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(PaddingHalf))
                Text(
                    text = record.title,
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}