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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Transformations
import androidx.lifecycle.coroutineScope
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.features.widget.HostActivity
import dev.gaborbiro.notes.features.widget.NotesWidgetsUpdater
import dev.gaborbiro.notes.ui.theme.NotesTheme
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import dev.gaborbiro.notes.ui.theme.PaddingHalf
import dev.gaborbiro.notes.util.BitmapLoader
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val recordsRepository by lazy { RecordsRepository.get() }
    private val mapper: RecordsUIMapper by lazy { RecordsUIMapper(BitmapLoader(this)) }

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
            verticalArrangement = Arrangement.spacedBy(PaddingHalf),
            state = listState,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            items(records.size, key = { records[it].recordId }) {
                Record(record = records[it], Modifier.animateItemPlacement())
            }
        }

        val showButton = remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0
            }
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(visible = showButton.value) {
                    Button(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(all = 16.dp),
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    ) {
                        Text(text = "Scroll to top")
                    }
                }
            }
        }
    }

    private val menuItems = listOf(
        PopUpMenuItem(
            id = "delete",
            icon = Icons.Outlined.Delete,
            label = "Delete"
        ),
        PopUpMenuItem(
            id = "update_image",
            icon = Icons.Outlined.Image,
            label = "Edit image"
        ),
        PopUpMenuItem(
            id = "delete_image",
            icon = Icons.Outlined.HideImage,
            label = "Delete image"
        ),
    )

    private fun onMenuItemSelected(
        record: RecordUIModel,
        menuId: String
    ) {
        when (menuId) {
            "delete" -> {
                lifecycle.coroutineScope.launch {
                    recordsRepository.delete(record.recordId)
                    NotesWidgetsUpdater.oneOffUpdate(this@MainActivity)
                }
            }

            "update_image" -> {
                HostActivity.launchRedoImage(this, record.templateId)
            }

            "delete_image" -> {
                lifecycle.coroutineScope.launch {
                    recordsRepository.updateTemplatePhoto(record.templateId, uri = null)
                    NotesWidgetsUpdater.oneOffUpdate(this@MainActivity)
                }
            }
        }
    }

    @Composable
    fun Record(record: RecordUIModel, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier
                .padding(start = PaddingDefault, end = PaddingDefault, top = PaddingDefault)
                .height(100.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                record.bitmap?.let { bitmap: Bitmap ->
                    Image(
                        painter = BitmapPainter(bitmap.asImageBitmap()),
                        contentScale = ContentScale.Crop,
                        contentDescription = "note image",
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(.2f)
                    )
                }
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(.8f)
                        .padding(top = PaddingDefault),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = PaddingDefault, end = PaddingDefault)
                            .weight(1f),
                        text = record.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.inverseSurface,
                    )
                    Row(
                        modifier = Modifier
                            .padding(start = PaddingDefault)
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = record.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(bottom = PaddingDefault),
                        )
                        Spacer(Modifier.weight(1f))
                        PopUpMenuButton(
                            options = menuItems,
                            action = {
                                onMenuItemSelected(record, it)
                            },
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PopUpMenuButton(
        options: List<PopUpMenuItem>,
        action: (String) -> Unit,
        modifier: Modifier
    ) {

        val expanded = remember { mutableStateOf(false) }

        Column {
            Box(modifier = Modifier.size(36.dp)) {
                IconButton(onClick = {
                    expanded.value = expanded.value.not()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_vert),
                        contentDescription = "overflow menu",
                        modifier = Modifier.padding(horizontal = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Box(modifier = modifier) {
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.inverseSurface)
                ) {
                    options.forEachIndexed { _, item ->
                        DropdownMenuItem(
                            onClick = {
                                expanded.value = false
                                action(item.id)
                            },
                            text = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.background,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = MaterialTheme.colorScheme.background,
                                )
                            }
                        )
                        if (item.hasBottomDivider) {
                            Divider()
                        }
                    }
                }
            }
        }
    }

    data class PopUpMenuItem(
        val id: String,
        val label: String,
        val icon: ImageVector,
        val hasBottomDivider: Boolean = false,
    )
}