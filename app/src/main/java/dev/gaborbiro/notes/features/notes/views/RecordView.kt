package dev.gaborbiro.notes.features.notes.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import dev.gaborbiro.notes.ui.theme.PaddingQuarter


@Composable
fun RecordView(
    modifier: Modifier = Modifier,
    record: RecordUIModel,
    onDuplicateRecord: () -> Unit,
    onUpdateImage: () -> Unit,
    onDeleteImage: () -> Unit,
    onEditRecord: () -> Unit,
    onDeleteRecord: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(start = PaddingDefault, end = PaddingDefault)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecordImage(record.bitmap, Modifier.size(64.dp))
        Spacer(modifier = Modifier.size(PaddingDefault))
        TitleAndSubtitle(
            modifier = Modifier
                .wrapContentHeight()
                .padding(end = PaddingDefault)
                .weight(1f),
            record = record
        )
        PopupMenu(onDuplicateRecord, onUpdateImage, onDeleteImage, onEditRecord, onDeleteRecord)
    }
}

@Composable
private fun RecordImage(bitmap: Bitmap?, modifier: Modifier) {
    bitmap?.let {
        Image(
            painter = BitmapPainter(bitmap.asImageBitmap()),
            contentScale = ContentScale.Crop,
            contentDescription = "note image",
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
        )
    } ?: run {
        Spacer(modifier)
    }
}

@Composable
private fun TitleAndSubtitle(modifier: Modifier, record: RecordUIModel) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = record.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = record.timestamp,
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(top = PaddingQuarter)
        )
    }
}

@Composable
private fun PopupMenu(
    onDuplicateRecord: () -> Unit,
    onUpdateImage: () -> Unit,
    onDeleteImage: () -> Unit,
    onEditRecord: () -> Unit,
    onDeleteRecord: () -> Unit,
) {
    PopUpMenuButton(
        options = listOf(
            PopUpMenuItem(
                icon = Icons.Outlined.FileCopy,
                label = "Copy record",
                onMenuItemSelected = onDuplicateRecord,
                hasBottomDivider = true,
            ),
            PopUpMenuItem(
                icon = Icons.Outlined.Image,
                label = "Update image",
                onMenuItemSelected = onUpdateImage,
            ),
            PopUpMenuItem(
                icon = Icons.Outlined.HideImage,
                label = "Delete image",
                onMenuItemSelected = onDeleteImage,
            ),
            PopUpMenuItem(
                icon = Icons.Outlined.Edit,
                label = "Edit",
                hasBottomDivider = true,
                onMenuItemSelected = onEditRecord,
            ),
            PopUpMenuItem(
                icon = Icons.Outlined.Delete,
                label = "Delete",
                onMenuItemSelected = onDeleteRecord,
            ),
        ),
    )
}

@Composable
private fun PopUpMenuButton(
    options: List<PopUpMenuItem>,
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

        Box {
            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                options.forEachIndexed { _, item ->
                    DropdownMenuItem(
                        onClick = {
                            expanded.value = false
                            item.onMenuItemSelected()
                        },
                        text = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    )
                    if (item.hasBottomDivider) {
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

data class PopUpMenuItem(
    val label: String,
    val icon: ImageVector,
    val hasBottomDivider: Boolean = false,
    val onMenuItemSelected: () -> Unit,
)