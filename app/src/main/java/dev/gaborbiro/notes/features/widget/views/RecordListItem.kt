package dev.gaborbiro.notes.features.widget.views

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.ui.theme.PaddingWidgetDefault
import dev.gaborbiro.notes.ui.theme.PaddingWidgetHalf

@Composable
fun RecordListItem(
    record: RecordUIModel,
    tapActionProvider: Action,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = PaddingWidgetHalf, horizontal = PaddingWidgetDefault)
            .clickable(tapActionProvider)
    ) {
        record.bitmap
            ?.let { image: Bitmap ->
                Image(
                    provider = ImageProvider(image),
                    contentDescription = "note image",
                    modifier = GlanceModifier
                        .size(WidgetImageSize),
                    contentScale = ContentScale.Crop,
                )
            }
            ?: run {
                Spacer(modifier = GlanceModifier.size(WidgetImageSize))
            }
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(start = PaddingWidgetDefault),
            verticalAlignment = Alignment.Vertical.Top,
        ) {
            Text(
                text = record.title,
                maxLines = 2,
                style = titleTextStyle,
            )
            Text(
                modifier = GlanceModifier
                    .fillMaxWidth(),
                text = record.timestamp,
                style = dateTextStyle,
            )
        }
    }
}
