package dev.gaborbiro.notes.features.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.Alignment
import androidx.glance.layout.Alignment.Companion.CenterVertically
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.ui.theme.PaddingDefaultWidget
import dev.gaborbiro.notes.ui.theme.PaddingHalfWidget
import dev.gaborbiro.notes.util.showActionNotification

@Composable
fun NotesWidgetContent(
    records: List<RecordUIModel>,
    modifier: GlanceModifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Records(
            records = records,
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
        )
        WaterWidgetButtonLayout(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .wrapContentHeight()
        )
    }
}

@Composable
fun Records(
    records: List<RecordUIModel>,
    modifier: GlanceModifier
) {
    LazyColumn(modifier) {
        items(records.size, itemId = { it.toLong() }) {
            Record(record = records[it])
        }
    }
}

@Composable
fun Record(record: RecordUIModel) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = PaddingHalfWidget)
            .clickable(
                actionRunCallback<DuplicateNoteAction>(
                    actionParametersOf(
                        ActionParameters.Key<Long>(PREFS_KEY_RECORD) to record.recordId
                    )
                )
            )
    ) {
        record.bitmap
            ?.let { image: Bitmap ->
                Image(
                    provider = ImageProvider(image),
                    contentDescription = "note image",
                    modifier = GlanceModifier
                        .size(60.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            ?: run {
                Spacer(modifier = GlanceModifier.size(60.dp))
            }
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(horizontal = PaddingDefaultWidget),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Text(
                text = record.title,
                modifier = GlanceModifier,
                maxLines = 2,
                style = RecordTitleTextStyle
            )
            Text(
                text = record.timestamp,
                style = RecordDateTextStyle,
                modifier = GlanceModifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun WaterWidgetButtonLayout(
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically
    ) {
        Image(
            provider = ImageProvider(
                resId = R.drawable.ic_add_photo
            ),
            contentDescription = "New note with camera",
            modifier = GlanceModifier
                .defaultWeight()
                .padding(vertical = 10.dp)
                .clickable(actionRunCallback<AddNoteWithCameraAction>())
        )
        Image(
            provider = ImageProvider(
                resId = R.drawable.ic_add_picture
            ),
            contentDescription = "New note from gallery",
            modifier = GlanceModifier
                .defaultWeight()
                .padding(vertical = 10.dp)
                .clickable(actionRunCallback<AddNoteWithImageAction>())
        )
        Image(
            provider = ImageProvider(
                resId = R.drawable.ic_add
            ),
            contentDescription = "New note",
            modifier = GlanceModifier
                .defaultWeight()
                .padding(vertical = 10.dp)
                .clickable(actionRunCallback<AddNoteAction>())
        )
    }
}

class AddNoteWithCameraAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HostActivity.launchAddNoteWithCamera(context)
    }
}

class AddNoteWithImageAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HostActivity.launchAddNoteWithImage(context)
    }
}

class AddNoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HostActivity.launchAddNote(context)
    }
}

class DuplicateNoteAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val recordId = parameters[ActionParameters.Key<Long>(PREFS_KEY_RECORD)]!!
        val newRecordId = RecordsRepository.get(context).duplicateRecord(recordId, "")
        NotesWidgetsUpdater.oneOffUpdate(context)
        context.showActionNotification(
            title = "Undo - duplicate record",
            action = "Undo",
            actionIcon = R.drawable.ic_undo,
            actionIntent = HostActivity.getDeleteIntent(context, newRecordId)
        )
    }
}

val RecordTitleTextStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    textAlign = TextAlign.Start,
    color = ColorProvider(Color.White)
)

val RecordDateTextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    textAlign = TextAlign.Start,
    color = ColorProvider(Color.LightGray)
)

private const val PREFS_KEY_RECORD = "recordId"